/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import com.github.gigurra.scalego.core.{Entity, System}
import com.github.gigurra.scalego.serialization.KnownSubTypes
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.{randomEntityID, _}
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.GameLoopModule._



/*
 * 'Main' class for the asteroids module in jukebox.
 * Handles the systems for the asteroids game as well as top-level variables (score & lives remaining)
 */
class AsteroidsModule extends ComponentModule {
  override def subtypes: KnownSubTypes = KnownSubTypes.empty +
    ("scoreRenderer" -> classOf[ScoreRenderer]) +
    ("backgroundRenderer" -> classOf[BackgroundRenderer])

  // TODO lives / score renderers?
  // background renderer

  override def makeSystems(): List[System[_, UUIDIdType]] = {
    Nil
  }

  override def systemsMadeCallback(systemsMade: List[System[_, UUIDIdType]]): Unit = {
  }

  def onLoad(): Unit = {
    val scoreRenderer = Entity.Builder + ScoreRenderer() build randomEntityID
    val backgroundRenderer = Entity.Builder + BackgroundRenderer() build randomEntityID

  }

  var score: Int = 0
  var playerLives: Int = STARTING_PLAYER_LIVES.getValueOption.getOrElse(3)

  AsteroidsModule.instance = this
}

object AsteroidsModule {
  var instance: AsteroidsModule = null
}
