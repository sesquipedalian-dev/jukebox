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
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.ObjectsModule._
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.{SceneObject, SceneObjectRenderer}
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D

import scala.util.Random



/*
 * 'Main' class for the asteroids module in jukebox.
 * Handles the systems for the asteroids game as well as top-level variables (score & lives remaining)
 */
class AsteroidsModule extends ComponentModule {
  override def subtypes: KnownSubTypes = KnownSubTypes.empty +
    ("scoreRenderer" -> classOf[ScoreRenderer]) +
    ("backgroundRenderer" -> classOf[BackgroundRenderer]) +
    ("startRenderer" -> classOf[StartRenderer]) +
    ("waitForStartUpdater" -> classOf[WaitForStartUpdater]) +
    ("movesBehaviour" -> classOf[MovesBehaviour])

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

    // set up inputs
    KEY_MAP.value = Some(KEY_MAP.getValue ++ Map(
      KeyCode.LEFT -> "TurnLeft",
      KeyCode.RIGHT -> "TurnRight",
      KeyCode.ENTER -> "Shoot",
      KeyCode.SPACE -> "Shoot"
    ))

    // set up wait for start button
    val waitForStartUpdater = Entity.Builder + WaitForStartUpdater() build randomEntityID
  }

  // globally accessible data in this module
  var score: Int = 0
  var playerLives: Int = STARTING_PLAYER_LIVES.getValueOption.getOrElse(3)
  var startRenderer: Option[UUIDIdType#EntityId] = None

  AsteroidsModule.instance = this


  def spawnAsteroid(): Unit = {
    // calculate a random initial velocity / direction
    val initialSpeed = Random.nextFloat() + 1.0
    val randomDirection = Random.nextFloat() * 2 * Math.PI
    val velocityX = Math.cos(randomDirection) * initialSpeed
    val velocityY = Math.sin(randomDirection) * initialSpeed

    // TODO calculate a random initial position
    val position = List[SerializablePoint2D](
      SerializablePoint2D(100, 100),
      SerializablePoint2D(100, 200),
      SerializablePoint2D(200, 200),
      SerializablePoint2D(200, 100)
    )
    // TODO no asteroids bounding box: 630, 350 // 970, 550

    // build the entity
    val newAsteroid = Entity.Builder +
      SceneObject(
        position,
        Some("img/mediumAsteroid.jpg"),
        None,
        None,
        0
      ) +
      MovesBehaviour(SerializablePoint2D(velocityX, velocityY)) +
      SceneObjectRenderer() build randomEntityID
  }
}

object AsteroidsModule {
  var instance: AsteroidsModule = null
}
