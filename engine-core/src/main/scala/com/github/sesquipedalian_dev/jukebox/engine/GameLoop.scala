/**
  * Copyright 11/18/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine

import java.io.{BufferedReader, FileReader}
import java.util.UUID
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.stage.WindowEvent

import com.github.gigurra.scalego.core.ECS
import com.github.gigurra.scalego.serialization.json.JsonSerializer
import com.github.gigurra.scalego.serialization.{IdTypeMapper, KnownSubTypes}
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.{GameLoopModule, Renderer, Updater}
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.GameLoopModule._
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.ObjectsModule._
import com.github.sesquipedalian_dev.jukebox.engine.components.objects._
import com.github.sesquipedalian_dev.jukebox.engine.modules.ModuleController
import com.github.sesquipedalian_dev.jukebox.engine.ui._
import com.github.sesquipedalian_dev.util.config._
import com.github.sesquipedalian_dev.util.ecs.{SerializablePoint2D, USER_SAVES_LOC}
import com.github.sesquipedalian_dev.util.scalafx._
import com.typesafe.scalalogging.LazyLogging

import scalafx.scene.paint.Color

sealed trait GameLoopState
case object START extends GameLoopState
case object RUNNING extends GameLoopState
case object TERMINATING extends GameLoopState
case object TERMINATED extends GameLoopState
case object PAUSED extends GameLoopState

case object MS_PER_UPDATE extends LoadableIntConfigSetting {
  override def configFileName: String = "tycoon.targetFrameDuration"
  override def defaultValue: Int = 16666666 // ms; 60 fps; fixed game update interval
}

/**
  * Adapted from http://gameprogrammingpatterns.com/game-loop.html
  * The above URL gives an outline for what to do in a frame in a core game loop.
  * We adapt it here to run a frame when JavaFX yields as part of an 'animation' frame.
  *
  */
class GameLoop() extends EventHandler[ActionEvent] with LazyLogging  {
  implicit var ecs: ECS[UUIDIdType] = null
  implicit var gameLoop: GameLoop = this

  def canvas: Option[Canvas] = Main.theScene.lookup("#canvas") match {
    case null => None
    case c: Canvas => Some(c)
    case n => {
      logger.warn("Couldn't find canvas! {}", n)
      None
    }
  }

  var state: GameLoopState = START
  var lastTime: Long = 0L
  var lag: Long = 0L

  def pause(): Unit = {
    state = PAUSED
  }

  def unpause(): Unit = {
    state = RUNNING
    // lastTime = System.currentTimeMillis() // make sure we don't accidentally run a bunch of frames after getting back from pause TODO unsure if this is necessary
  }

  override def handle(event: ActionEvent): Unit = {
    state match {
      case PAUSED => {
        // nop
      }
      case START => {
        init()
      }
      case RUNNING => {
        val loopStart = System.nanoTime()
        val elapsed = if(lastTime == 0) { 0 } else { loopStart - lastTime }
        lastTime = loopStart
        lag += elapsed

        // when crank turns, check how many ticks of the game engine we need to execute
        while(lag >= MS_PER_UPDATE()) {
          update()
          lag -= MS_PER_UPDATE()
        }

        // render the game state we have
        render()
      }
      case TERMINATING =>  {
        cleanup()
      }
      case TERMINATED => {
        // do nothing if we're done
      }
    }
  }

  def makeSureAllConfigLoaded(): Unit = {
    USER_SAVES_LOC.getValueOption
  }

  def init(): Unit = {
    makeSureAllConfigLoaded()

    logger.info("GameLoop init!")
    components.addComponent(ObjectsModule)
    components.addComponent(GameLoopModule)
    val (cb, systems) = components.makeNewECSSystems()
    cb()
    ecs = ECS[UUIDIdType](systems:_*)
    state = RUNNING

    ConfigSettings.loadConfig()

    // initialize / load various components
    FpsCounter()
    PreferencesModal()
    SaveModal()
    LoadModal()
    AboutModal()
    Quicksave()
    SceneController()
    InputManager()
    I18nManager()
    ModuleController()

    // also intercept alt-f4 and 'x' button on app
    Main.stage.setOnCloseRequest(new EventHandler[WindowEvent]() {
      override def handle(event: WindowEvent): Unit = {
        event.consume()
        state = TERMINATING
      }
    })

    // assign close behaviour to application exit menu item
    MenuLookup.oneLevelLookup(Main.stage.scene(), "fileMenu", "exitBtn").foreach(menuItem => {
      menuItem.setOnAction(new EventHandler[ActionEvent]() {
        override def handle(event: ActionEvent): Unit = {
          state = TERMINATING
        }
      })
    })
  }

  def cleanup(): Unit = {
    logger.info("GameLoop cleanup!")
    // TODO entity / components may want a chance to cleanup
    state = TERMINATED
    Main.stage.close()
  }

  // update game engine one tick
  def update(): Unit = {
    logger.trace("update loop")

    preUpdateCallbacks.sortBy(_._1).foreach(f => f._2(ecs))

    ecs.system[Updater].foreach(p => {
      val (eid, updaterList) = p
      updaterList.foreach(updater => {
        logger.trace("eid is upating {}", eid)
        updater.update(eid)
      })
    })

    postUpdateCallbacks.sortBy(_._1).foreach(f => f._2(ecs))
  }

  // update game properties for rendering every turn
  def render(): Unit = {
    canvas.foreach(c => {
      val gc = c.getGraphicsContext2D

      renderCallbacks.sortBy(_._1).foreach(f => f._2(gc, ecs))

      ecs.system[Renderer].toList.flatMap(p => {
        p._2.map(v => (p._1 -> v))
      }).sortBy(p => {
        val (eid, renderer) = p
        renderer.renderOrder(ecs, eid)
      }).foreach(p => {
        // TODO only render the object if it is within the viewport?
        val (eid, renderer) = p
        logger.trace("rendering ent {}", eid)
        renderer.render(eid, gc)
      })
    })
  }

  /*
   * allow callbacks in update loop - this is easier and cleaner than making these funcs
   * be ECS members so that we don't have to muck with reloading them
   */
  var preUpdateCallbacks: List[(Int, (ECS[UUIDIdType]) => Unit)] = Nil
  def onPreUpdate(func: (ECS[UUIDIdType]) => Unit, sortOrder: Int): Unit = preUpdateCallbacks :+= (sortOrder, func)

  var postUpdateCallbacks: List[(Int, (ECS[UUIDIdType]) => Unit)] = Nil
  def onPostUpdate(func: (ECS[UUIDIdType]) => Unit, sortOrder: Int): Unit = postUpdateCallbacks :+= (sortOrder, func)

  var renderCallbacks: List[(Int, (GraphicsContext, ECS[UUIDIdType]) => Unit)] = Nil
  def onRender(func: (GraphicsContext, ECS[UUIDIdType]) => Unit, sortOrder: Int): Unit = renderCallbacks :+= (sortOrder, func)
}


