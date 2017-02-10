/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import javafx.scene.input.KeyCode

import com.github.gigurra.scalego.core.{ECS, Entity, System}
import com.github.gigurra.scalego.serialization.KnownSubTypes
import com.github.sesquipedalian_dev.jukebox.engine._
import com.github.sesquipedalian_dev.jukebox.engine.components.{randomEntityID, _}
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.GameLoopModule._
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Updater
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.ObjectsModule._
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.{SceneObject, SceneObjectRenderer}
import com.github.sesquipedalian_dev.util.collections.ListExtensions
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
      AsteroidsGlobalController(GlobalControllerState.READY_TO_START) build randomEntityID
  }

  // globally accessible data in this module
  AsteroidsModule.instance = this

  def spawnBullet(source: SceneObject, direction: SerializablePoint2D): Unit = {
    val initialPosition = source.polygon.head
    spawnBullet(initialPosition, direction)
  }

  def spawnBullet(initialPosition: SerializablePoint2D, direction: SerializablePoint2D): Unit = {
    // calculate a initial velocity / direction
    val initialSpeed = Random.nextFloat() + 4.0
    val velocity = direction * initialSpeed

    // initial position
    val position = List[SerializablePoint2D](
      SerializablePoint2D(initialPosition.x, initialPosition.y),
      SerializablePoint2D(initialPosition.x + 10, initialPosition.y),
      SerializablePoint2D(initialPosition.x + 10, initialPosition.y + 10),
      SerializablePoint2D(initialPosition.x, initialPosition.y + 10)
    )

    // build the entity
    val newAsteroid = Entity.Builder +
      SceneObject(
        polygon = position,
        texturePath = None,
        currentAnimation = None,
        mouseOverText = None,
        zSort = 0
      ) +
      MovesBehaviour(destroyAfterDistance = Some(800.0), SerializablePoint2D(velocity.x, velocity.y)) +
      BulletRenderer() build randomEntityID
  }

  def spawnAsteroid(
    _params: Option[AsteroidParams] = None,
    _initialPosition: Option[SerializablePoint2D] = None,
    directionRadians: Option[Double] = None
  )(implicit ecs: ECS[UUIDIdType]): UUIDIdType#EntityId = {
    logger.info("SPAWNING ASTEROID")
    // pick a random asteroid type to spawn?
    // TODO should be biased towards medium sized, then larger, then smallest
    val params = _params.getOrElse(Random.shuffle(ASTEROID_PARAMS_MAP().values).head)

    // calculate a random initial velocity / direction
    val initialSpeed = Random.nextFloat() + params.initialVelocityScale
    val randomDirection = directionRadians.getOrElse(Random.nextFloat() * 2 * Math.PI)
    val velocityX = Math.cos(randomDirection) * initialSpeed
    val velocityY = Math.sin(randomDirection) * initialSpeed
    logger.info("making a new asteroid; initial speed {" + initialSpeed + "} initial direction = {" + randomDirection + "}")

    // either use provided initial position or calculate a random one
    val initialPosition = _initialPosition.getOrElse(randomAsteroidPosition(params.size, ecs))
    val polygon = List[SerializablePoint2D](
      SerializablePoint2D(initialPosition.x, initialPosition.y),
      SerializablePoint2D(initialPosition.x + params.width, initialPosition.y),
      SerializablePoint2D(initialPosition.x + params.width, initialPosition.y + params.height),
      SerializablePoint2D(initialPosition.x, initialPosition.y + params.height)
    )

    // build the entity
    val newAsteroid = Entity.Builder +
      SceneObject(
        polygon,
        texturePath = Some(params.spriteImg),
        currentAnimation = None,
        mouseOverText = None,
        zSort = 0
      ) +
      MovesBehaviour(destroyAfterDistance = None, SerializablePoint2D(velocityX, velocityY)) +
      SceneObjectRenderer() +
      AsteroidCollisionWatcher(params.size) build randomEntityID

    newAsteroid.id
  }

  def spawnPlayer(): Unit = {
    val playerEnt = Entity.Builder +
      Player(
        rotationRadians = -Math.PI / 2,
        livesRemaining  = 3,
        score = 0
      ) +
      PlayerRenderer() build randomEntityID
  }

  private val playerExclusionRanges: Set[(Int, Int)] = {
    ListExtensions.cartesianProduct((630 to 970).toList, (350 to 550).toList).toSet
  }

  private def randomAsteroidPosition(size: String, ecs: ECS[UUIDIdType]): SerializablePoint2D = {
    if(allPotentialAsteroidPositions.getOrElse(size, List[(Int, Int)]()).isEmpty) {
      GameLoop.instance.pause()

      val params = ASTEROID_PARAMS_MAP().get(size).get

      // construct initial range that contains the entire game area
      val initialXRange = 0 to (CANVAS_WIDTH() - params.width).toInt
      val initialYRange = 0 to (CANVAS_HEIGHT() - params.height).toInt
      val wholeRange = ListExtensions.cartesianProduct(initialXRange.toList, initialYRange.toList).toSet

      val holes =
        // collect all scene objects that have an AsteroidCollisionWatcher (they're an asteroid)
        ecs.system[SceneObject].toList.flatMap(kvp => kvp._2.map(v => kvp._1 -> v)).collect({
          case (eid, so) if ecs.system[Updater].getOrElse(eid, Nil).collect({case u: AsteroidCollisionWatcher => u}).nonEmpty => {
            so
          }
        })
        // for each asteroid object, make a hole in the range for its polygon size
        .flatMap(so => {
          val xRange = so.polygon.head.x.toInt to so.polygon.drop(1).head.x.toInt
          val yRange = so.polygon.head.y.toInt to so.polygon.last.y.toInt
          ListExtensions.cartesianProduct(xRange.toList, yRange.toList)
        })
        .toSet

      // construct ranges that don't include the holes

      // subtract exclusion ranges so that no asteroids start too close to player or on other asteroids
      allPotentialAsteroidPositions += (size -> Random.shuffle((wholeRange diff playerExclusionRanges diff holes).toList))

      GameLoop.instance.unpause()
    }

    val lst = allPotentialAsteroidPositions.getOrElse(size, List())
    val retval = lst.head
    allPotentialAsteroidPositions += (size -> lst.tail)

    SerializablePoint2D(retval._1, retval._2)
  }

  private var allPotentialAsteroidPositions: Map[String, List[(Int, Int)]] = Map()
}

object AsteroidsModule {
  var instance: AsteroidsModule = null
  implicit var playerSystem: System[Player, UUIDIdType] = new System[Player, UUIDIdType]("playerSystem")
}
