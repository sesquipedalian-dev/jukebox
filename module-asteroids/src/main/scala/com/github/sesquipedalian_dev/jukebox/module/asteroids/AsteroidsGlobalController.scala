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
  var state: GlobalControllerState
) extends Updater
  with LazyLogging
{
  override def update(eid: EntityIdType)(implicit ecs: ECS[UUIDIdType]): Unit = {
    logger.info("asteroids module global input handler {} {}", InputManager.gameTickInputs, state)
    if(InputManager.gameTickInputs.contains("Shoot_DOWN")) {
      state match {
        case READY_TO_START => {
          state = PLAYING

          // testing
          val asteroidId = AsteroidsModule.instance.spawnAsteroid(
            directionRadians = Some(Math.PI)
          )
          val asteroidSceneObject = ecs.system[SceneObject].getOrElse(asteroidId, Nil).headOption

          AsteroidsModule.instance.spawnBullet(
            source = asteroidSceneObject.get,
            direction = SerializablePoint2D(1.0, 0)
          )

          asteroidSceneObject.get.polygon = asteroidSceneObject.get.polygon.map(p => {
            SerializablePoint2D(p.x + 500, p.y)
          })
          // DONE testing

          AsteroidsModule.instance.spawnPlayer()
        }
        case PLAYING => {
          ecs.system[Player].toList.flatMap(_._2).foreach(player => {
            // TODO spawn bullets
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
