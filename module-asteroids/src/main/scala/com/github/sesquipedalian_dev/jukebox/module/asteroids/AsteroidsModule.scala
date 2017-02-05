/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import javafx.scene.input.KeyCode

import com.github.gigurra.scalego.core.{Entity, System}
import com.github.gigurra.scalego.serialization.KnownSubTypes
import com.github.sesquipedalian_dev.jukebox.engine.{KEY_MAP, UUIDIdType}
import com.github.sesquipedalian_dev.jukebox.engine.components.{randomEntityID, _}
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.GameLoopModule._



/*
 * 'Main' class for the asteroids module in jukebox.
 * Handles the systems for the asteroids game as well as top-level variables (score & lives remaining)
 */
class AsteroidsModule extends ComponentModule {
  override def subtypes: KnownSubTypes = KnownSubTypes.empty +
    ("scoreRenderer" -> classOf[ScoreRenderer]) +
    ("backgroundRenderer" -> classOf[BackgroundRenderer]) +
    ("startRenderer" -> classOf[StartRenderer]) +
    ("waitForStartUpdater" -> classOf[WaitForStartUpdater])

  // TODO lives / score renderers?
  // background renderer

  override def makeSystems(): List[System[_, UUIDIdType]] = {
    Nil
  }

  override def systemsMadeCallback(systemsMade: List[System[_, UUIDIdType]]): Unit = {
  }

  def onLoad(): Unit = {
    // set up some basic screen objects
    val backgroundRenderer = Entity.Builder + BackgroundRenderer() build randomEntityID
    val scoreRenderer = Entity.Builder + ScoreRenderer() build randomEntityID
    val startRendererEnt = Entity.Builder + StartRenderer() build randomEntityID
    startRenderer = Some(startRendererEnt.id)

    val waitForStartUpdater = Entity.Builder + WaitForStartUpdater() build randomEntityID

    // set up inputs
    KEY_MAP.value = Some(KEY_MAP.getValue ++ Map(
      KeyCode.LEFT -> "TurnLeft",
      KeyCode.RIGHT -> "TurnRight",
      KeyCode.ENTER -> "Shoot",
      KeyCode.SPACE -> "Shoot"
    ))

    // set up wait for start button
  }

  // globally accessible data in this module
  var score: Int = 0
  var playerLives: Int = STARTING_PLAYER_LIVES.getValueOption.getOrElse(3)
  var startRenderer: Option[UUIDIdType#EntityId] = None

  AsteroidsModule.instance = this
}

object AsteroidsModule {
  var instance: AsteroidsModule = null
}
