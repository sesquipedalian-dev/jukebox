/**
  * Copyright 2/5/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.{InputManager, UUIDIdType}
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Updater
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.SceneObject
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.ObjectsModule._
import com.github.sesquipedalian_dev.jukebox.module.asteroids.AsteroidsModule._
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D
import com.typesafe.scalalogging.LazyLogging

sealed trait GlobalControllerState
case object READY_TO_START extends GlobalControllerState
case object PLAYING extends GlobalControllerState
case object DIED extends GlobalControllerState

case class AsteroidsGlobalController(
  var state: GlobalControllerState,
  var framesToWaitBeforeSpawningMoreBullets: Int = 0,
  var framesSinceAsteroidSpawn: Int = 60
) extends Updater
  with LazyLogging
{
  override def update(eid: EntityIdType)(implicit ecs: ECS[UUIDIdType]): Unit = {
    logger.trace("asteroids module global input handler {} {} {" + framesSinceAsteroidSpawn + "}", InputManager.gameTickInputs, state)
    if(framesToWaitBeforeSpawningMoreBullets > 0) { framesToWaitBeforeSpawningMoreBullets -= 1 }

    // check asteroid spawner
    state match {
      case PLAYING => {
        if (framesSinceAsteroidSpawn > 0) {
          framesSinceAsteroidSpawn -= 1
        }

        if (framesSinceAsteroidSpawn <= 0) {
          AsteroidsModule.instance.spawnAsteroid()
          framesSinceAsteroidSpawn = Math.max(90, FRAMES_BETWEEN_ASTEROID_SPAWN())
          // increase spawn rate over time
          FRAMES_BETWEEN_ASTEROID_SPAWN.value = Some(
            (FRAMES_BETWEEN_ASTEROID_SPAWN.value.getOrElse(FRAMES_BETWEEN_ASTEROID_SPAWN()) * .75).toInt
          )
        }
      }
      case _ =>
    }

    // check player inputs
    if(InputManager.gameTickInputs.contains("Shoot_DOWN")) {
      state match {
        case READY_TO_START => {
          state = PLAYING

          for { i <- 1 to INITIAL_ASTEROID_COUNT() } {
            AsteroidsModule.instance.spawnAsteroid()
          }

          AsteroidsModule.instance.spawnPlayer()
        }
        case PLAYING => {
          ecs.system[Player].toList.flatMap(_._2).foreach(player => {
            if(framesToWaitBeforeSpawningMoreBullets <= 0) {
              val direction = SerializablePoint2D(1, 0).rotate(player.rotationRadians)
              AsteroidsModule.instance.spawnBullet(player.position, direction)

              framesToWaitBeforeSpawningMoreBullets = MIN_FRAMES_BETWEEN_BULLETS()
            }
          })
        }
        case DIED => {
          // TODO restart game
        }
      }
    } else if (InputManager.gameTickInputs.contains("TurnLeft_DOWN")) {
      state match {
        case PLAYING => {
          ecs.system[Player].toList.flatMap(_._2).foreach(player => {
            player.rotationRadians -= PLAYER_ROTATION_SPEED_RADIANS()
          })
        }
        case _ =>
      }
    } else if (InputManager.gameTickInputs.contains("TurnRight_DOWN")) {
      state match {
        case PLAYING => {
          ecs.system[Player].toList.flatMap(_._2).foreach(player => {
            player.rotationRadians += PLAYER_ROTATION_SPEED_RADIANS()
          })
        }
        case _ =>
      }
    }
  }
}
