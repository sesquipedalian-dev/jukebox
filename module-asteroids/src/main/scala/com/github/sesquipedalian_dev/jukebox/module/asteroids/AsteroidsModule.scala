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
import com.typesafe.scalalogging.LazyLogging

import scala.util.Random

/*
 * 'Main' class for the asteroids module in jukebox.
 * Handles the systems for the asteroids game as well as top-level variables (score & lives remaining)
 */
class AsteroidsModule extends ComponentModule with LazyLogging {
  import AsteroidsModule._

  override def subtypes: KnownSubTypes = KnownSubTypes.empty +
    ("playerRenderer" -> classOf[PlayerRenderer]) +
    ("backgroundRenderer" -> classOf[BackgroundRenderer]) +
    ("startRenderer" -> classOf[MessageTextRenderer]) +
    ("waitForStartUpdater" -> classOf[AsteroidsGlobalController]) +
    ("movesBehaviour" -> classOf[MovesBehaviour]) +
    ("bulletRenderer" -> classOf[BulletRenderer])


  override def makeSystems(): List[System[_, UUIDIdType]] = {
    val newPlayerSystem = new System[Player, UUIDIdType]("playerSystem")
    newPlayerSystem ::
      Nil
  }

  override def systemsMadeCallback(systemsMade: List[System[_, UUIDIdType]]): Unit = {
    AsteroidsModule.playerSystem = systemsMade.drop(0).head.asInstanceOf[System[Player, UUIDIdType]]
  }

  def onLoad(): Unit = {
    // set up some basic screen objects
    val backgroundRenderer = Entity.Builder + BackgroundRenderer() build randomEntityID

    // set up inputs
    KEY_MAP.value = Some(KEY_MAP.getValue ++ Map(
      KeyCode.LEFT -> "TurnLeft",
      KeyCode.RIGHT -> "TurnRight",
      KeyCode.ENTER -> "Shoot",
      KeyCode.SPACE -> "Shoot"
    ))

    // set up wait for start button
    val globalInputController = Entity.Builder +
      MessageTextRenderer() +
      AsteroidsGlobalController(READY_TO_START) build randomEntityID
  }

  // globally accessible data in this module
  AsteroidsModule.instance = this

  def spawnBullet(source: SceneObject, direction: SerializablePoint2D): Unit = {
    // calculate a initial velocity / direction
    val initialSpeed = Random.nextFloat() + 4.0
    val velocity = direction * initialSpeed

    // initial position
    val initialPosition = source.polygon.head
    val position = List[SerializablePoint2D](
      SerializablePoint2D(initialPosition.x, initialPosition.y),
      SerializablePoint2D(initialPosition.x + 10, initialPosition.y),
      SerializablePoint2D(initialPosition.x + 10, initialPosition.y + 10),
      SerializablePoint2D(initialPosition.x, initialPosition.y + 10)
    )

    // build the entity
    val newAsteroid = Entity.Builder +
      SceneObject(
        position,
        None,
        None,
        None,
        0
      ) +
      MovesBehaviour(destroyAfterDistance = Some(800.0), SerializablePoint2D(velocity.x, velocity.y)) +
      BulletRenderer() build randomEntityID
  }

  def spawnAsteroid(
    _params: Option[AsteroidParams] = None,
    _initialPosition: Option[SerializablePoint2D] = None,
    directionRadians: Option[Double] = None
  ): UUIDIdType#EntityId = {
    // pick a random asteroid type to spawn?
    val params = _params.getOrElse(Random.shuffle(ASTEROID_PARAMS_MAP().values).head)

    // calculate a random initial velocity / direction
    val initialSpeed = Random.nextFloat() + params.initialVelocityScale
    val randomDirection = directionRadians.getOrElse(Random.nextFloat() * 2 * Math.PI)
    val velocityX = Math.cos(randomDirection) * initialSpeed
    val velocityY = Math.sin(randomDirection) * initialSpeed
    logger.info("making a new asteroid; initial speed {" + initialSpeed + "} initial direction = {" + randomDirection + "}")

    // TODO calculate a random initial position
    // TODO no asteroids bounding box: 630, 350 // 970, 550 should keep them out of where the player is
    val initialPosition = _initialPosition.getOrElse(SerializablePoint2D(100, 100))
    val position = List[SerializablePoint2D](
      SerializablePoint2D(initialPosition.x, initialPosition.y),
      SerializablePoint2D(initialPosition.x + params.width, initialPosition.y),
      SerializablePoint2D(initialPosition.x + params.width, initialPosition.y + params.height),
      SerializablePoint2D(initialPosition.x, initialPosition.y + params.height)
    )

    // build the entity
    val newAsteroid = Entity.Builder +
      SceneObject(
        position,
        Some(params.spriteImg),
        None,
        None,
        0
      ) +
      MovesBehaviour(destroyAfterDistance = None, SerializablePoint2D(velocityX, velocityY)) +
      SceneObjectRenderer() +
      AsteroidCollisionWatcher(params.size) build randomEntityID

    newAsteroid.id
  }

  def spawnPlayer(): Unit = {
    val playerEnt = Entity.Builder +
      Player(
        rotationRadians = 90.0,
        livesRemaining  = 3,
        score = 0
      ) +
      PlayerRenderer() build randomEntityID
  }
}

object AsteroidsModule {
  var instance: AsteroidsModule = null
  implicit var playerSystem: System[Player, UUIDIdType] = new System[Player, UUIDIdType]("playerSystem")
}
