/**
  * Copyright 11/18/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine

import javafx.event.EventHandler
import javafx.scene.input.KeyCode

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.components.game.{SceneController, SceneObject}
import com.github.sesquipedalian_dev.jukebox.engine._
import com.github.sesquipedalian_dev.util.config.ConfigSetting
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.collection.immutable.Queue
import scalafx.event.EventType
import scalafx.geometry.Point2D
import scalafx.scene.input.{KeyEvent, MouseEvent}

case object KEY_MAP extends ConfigSetting[Map[KeyCode, String]] {
  override def defaultValue: Map[KeyCode, String] = Map(
    KeyCode.LEFT -> "LeftScroll",
    KeyCode.RIGHT -> "RightScroll",
    KeyCode.SHIFT -> "HighlightObjects"
  )

  override def populateFromConfig(newConfig: Config): Unit = {
    // TODO??
  }

  // TODO also UI? Let's take inspiration from something like SNES9x there, eventually
  // there's also remapping the KEY_MAP depending on context, hm...

  // TODO another possible ask would be to map mouse button inputs to virtual keys, which doesn't seem like it would
  // work with the map being KeyCode to virtual key ID? we would need Mouse button ID as well
}

// TODO - we want to capture input and store it in a queue as tokens
// for processing on our game loop at our desired rate
// dunno so much how to do that with Javafx platform
//
case object InputManager extends LazyLogging
{
  var gatheredKeyCodes: Queue[(KeyCode, EventType[javafx.scene.input.KeyEvent])] = Queue()
  var gameTickInputs: List[String] = Nil
  var gatheredMousePos: Option[Point2D] = None
  var currentMousePointer: Option[Point2D] = None
  var gatheredMouseEvents: Map[javafx.scene.input.MouseButton, Set[EventType[javafx.scene.input.MouseEvent]]] = Map()

  def apply()(implicit gameLoop: GameLoop): Unit = {
    val canvas = Main.theScene
    logger.trace("adding input handlers on canvas!")

    canvas.addEventHandler(KeyEvent.KeyPressed, new EventHandler[javafx.scene.input.KeyEvent]() {
      override def handle(event: javafx.scene.input.KeyEvent): Unit = {
        gatheredKeyCodes.synchronized {
          gatheredKeyCodes = gatheredKeyCodes :+ ((event.getCode, KeyEvent.KeyPressed))
        }
      }
    })

    canvas.addEventHandler(KeyEvent.KeyReleased, new EventHandler[javafx.scene.input.KeyEvent]() {
      override def handle(event: javafx.scene.input.KeyEvent): Unit = {
        gatheredKeyCodes.synchronized {
          gatheredKeyCodes = gatheredKeyCodes :+ ((event.getCode, KeyEvent.KeyReleased))
        }
      }
    })

    gameLoop.canvas.foreach(mouseCanvas => {
      mouseCanvas.addEventHandler(MouseEvent.MouseMoved, new EventHandler[javafx.scene.input.MouseEvent]() {
        override def handle(event: javafx.scene.input.MouseEvent): Unit = {
          gatheredMousePos.synchronized {
            gatheredMousePos = Some(new Point2D(event.getX(), event.getY()))
            logger.trace(s"mouse event all coord options ${event.getX}, ${event.getY}, ${event.getSceneX}, ${event.getSceneY}, ${event.getScreenX}, ${event.getScreenY}")
          }
        }
      })

      addMouseEventHandler(MouseEvent.MouseClicked, mouseCanvas)
      addMouseEventHandler(MouseEvent.MouseReleased, mouseCanvas)
      addMouseEventHandler(MouseEvent.MousePressed, mouseCanvas)
    })

    gameLoop.onPreUpdate((ecs) => preUpdate(gameLoop.canvas), -1000)
    gameLoop.onPostUpdate((ecs) => postUpdate(), 1000)
  }

  private def addMouseEventHandler(eventType: EventType[javafx.scene.input.MouseEvent], canvas: javafx.scene.Node): Unit = {
    canvas.addEventHandler(eventType, new EventHandler[javafx.scene.input.MouseEvent]() {
      override def handle(event: javafx.scene.input.MouseEvent): Unit = {
        gatheredMouseEvents.synchronized {
          gatheredMouseEvents += (event.getButton -> (gatheredMouseEvents.getOrElse(event.getButton, Set()) + eventType))
        }
      }
    })
  }

  def preUpdate(canvas: Option[javafx.scene.Node]): Unit = {
    logger.trace("input manager capturing key codes")
    // copy current key status from the place where they've been collected
    // and stick them in a stable list for consumption by other updaters
    gatheredKeyCodes.synchronized {
      logger.trace("input manager capturing key codes 2 {}", gatheredKeyCodes)
      gameTickInputs = gatheredKeyCodes.toList.flatMap(p => {
        val (kc, t) = p
        KEY_MAP.getValue.get(kc).map(virtualKey => { // convert to virtual keys
          val suffix = t match {
            case KeyEvent.KeyPressed => "_DOWN"
            case KeyEvent.KeyReleased => "_UP"
          }
          virtualKey + suffix
        })
      })
      gatheredKeyCodes = Queue()
    }

    // copy gathered mouse pos info to place where we can use in updates
    gatheredMousePos.synchronized {
      gatheredMousePos match {
        case Some(mousePos) => {
          canvas.foreach(node => {
            // adjust the mouse coords to our canvas coords. This means modifying by scaling ratio of the canvas
            // and then adding the viewport.
            currentMousePointer = Some(new Point2D(
              SceneController.viewport.head.x + mousePos.x,
              SceneController.viewport.head.y + mousePos.y
            ))
            gatheredMousePos = None
          })
        }
        case _ =>
      }
    }

    logger.trace("input manager capturing inputs for game tick {} {}", gameTickInputs, currentMousePointer)
  }

  def postUpdate(): Unit = {
    // clear this frame's inputs
    gameTickInputs.synchronized {
      gameTickInputs = List()
    }
    gatheredMouseEvents.synchronized {
      gatheredMouseEvents = Map()
    }
  }

  // TODO we could memoize this for a frame to improve performance
  def objectUnderMouse(ecs: ECS[UUIDIdType]): Option[ObjectUnderMouse] = {
    currentMousePointer.flatMap(mousePointer => {
      ecs.system[SceneObject].toList.sortBy(-_._2.zSort)
        .find(p => p._2.containsPoint(mousePointer))
        .map(p => {
          val (eid, obj) = p
          ObjectUnderMouse(
            eid,
            gatheredMouseEvents.toList.map(p => {
              val (button, events) = p
              MouseButtonStatus(
                button,
                events.contains(MouseEvent.MousePressed) || events.contains(MouseEvent.MouseClicked),
                events.contains(MouseEvent.MouseReleased) || events.contains(MouseEvent.MouseClicked)
              )
            })
          )
        })
    })
  }
}

case class MouseButtonStatus(
  id: javafx.scene.input.MouseButton,
  down: Boolean,
  up: Boolean
)

case class ObjectUnderMouse(
  eid: UUIDIdType#EntityId,
  buttonStatus: List[MouseButtonStatus]
)
