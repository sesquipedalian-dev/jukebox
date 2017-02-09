/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.snake

import javafx.scene.input.KeyCode

import com.github.gigurra.scalego.core.{Entity, System}
import com.github.gigurra.scalego.serialization.KnownSubTypes
import com.github.sesquipedalian_dev.jukebox.engine.components._
import com.github.sesquipedalian_dev.jukebox.engine.{KEY_MAP, MS_PER_UPDATE, UUIDIdType}
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D
import com.typesafe.scalalogging.LazyLogging
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.GameLoopModule._

/*
 * 'Main' class for the asteroids module in jukebox.
 * Handles the systems for the asteroids game as well as top-level variables (score & lives remaining)
 */
class SnakeModule extends ComponentModule with LazyLogging {
  import SnakeModule._

  override def subtypes: KnownSubTypes = KnownSubTypes.empty +
    ("playerRenderer" -> classOf[PlayerRenderer]) +
    ("backgroundRenderer" -> classOf[BackgroundRenderer]) +
    ("startRenderer" -> classOf[MessageTextRenderer]) +
    ("waitForStartUpdater" -> classOf[SnakeInputController]) +
    ("playerUpdater" -> classOf[PlayerUpdater])

  override def makeSystems(): List[System[_, UUIDIdType]] = {
    val newPlayerSystem = new System[Player, UUIDIdType]("playerSystem")
    newPlayerSystem ::
      Nil
  }

  override def systemsMadeCallback(systemsMade: List[System[_, UUIDIdType]]): Unit = {
    SnakeModule.playerSystem = systemsMade.drop(0).head.asInstanceOf[System[Player, UUIDIdType]]
  }

  def onLoad(): Unit = {
    // set up some basic screen objects
    val backgroundRenderer = Entity.Builder + BackgroundRenderer() build randomEntityID

    // set up inputs
    KEY_MAP.value = Some(KEY_MAP.getValue ++ Map(
      KeyCode.LEFT -> "GoLeft",
      KeyCode.RIGHT -> "GoRight",
      KeyCode.UP -> "GoUp",
      KeyCode.DOWN -> "GoDown",
      KeyCode.ENTER -> "Shoot",
      KeyCode.SPACE -> "Shoot"
    ))

    // set up wait for start button
    val globalInputController = Entity.Builder +
      MessageTextRenderer() +
      SnakeInputController(GlobalControllerState.READY_TO_START) build randomEntityID

    // this game ticks at much less than 60 fps
    MS_PER_UPDATE.value = Some(16666666 /*60fps*/ * 60)
  }

  // globally accessible data in this module
  SnakeModule.instance = this

  def spawnPlayer(): Unit = {
    val startY = CANVAS_PIXELS_HEIGHT() / 2
    val startX = CANVAS_PIXELS_WIDTH() / 2
    val playerEnt = Entity.Builder +
      Player(
        segments = List(
          SerializablePoint2D(startX, startY),
          SerializablePoint2D(startX, startY + 1),
          SerializablePoint2D(startX, startY + 2)
        )
      ) +
      PlayerRenderer() +
      PlayerUpdater() build randomEntityID
  }
}

object SnakeModule {
  var instance: SnakeModule = null
  implicit var playerSystem: System[Player, UUIDIdType] = new System[Player, UUIDIdType]("playerSystem")
}
