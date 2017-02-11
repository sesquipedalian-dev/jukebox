/**
  * Copyright 2/5/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.snake

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.GameLoopModule._
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.{Renderer, Updater}
import com.github.sesquipedalian_dev.jukebox.engine.{InputManager, UUIDIdType}
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D
import com.typesafe.scalalogging.LazyLogging
import com.github.sesquipedalian_dev.jukebox.module.snake.SnakeModule._

object GlobalControllerState {
  val READY_TO_START = "ready"
  val PLAYING = "steady"
  val DIED = "stop"
}

case class SnakeInputController(
  var state: String
) extends Updater
  with LazyLogging
{
  override def update(eid: EntityIdType)(implicit ecs: ECS[UUIDIdType]): Unit = {
    // check player inputs
    if(InputManager.gameTickInputs.contains("Shoot_UP")) {
      state match {
        case GlobalControllerState.READY_TO_START => {
          SnakeModule.instance.spawnPlayer()

          SnakeModule.instance.spawnPellet(ecs)

          state = GlobalControllerState.PLAYING

        }
        case GlobalControllerState.PLAYING => {
          // NOP
        }
        case GlobalControllerState.DIED => {
          ecs.system[Player].foreach(kvp => ecs -= kvp._1) // the player is dead
          SnakeModule.instance.spawnPlayer() // long live the player!

          ecs.system[Renderer].toList.flatMap(kvp => kvp._2.map(v => kvp._1 -> v)).foreach({
            case (k, x: PelletRenderer) => {
              ecs -= k
            }
            case _ =>
          })
          SnakeModule.instance.spawnPellet(ecs)

          state = GlobalControllerState.PLAYING
        }
      }
    }

    if (InputManager.gameTickInputs.contains("GoLeft_UP")) {
      state match {
        case GlobalControllerState.PLAYING => {
          ecs.system[Player].toList.flatMap(_._2).foreach(player => {
            if((player.directionOfTravel == Direction.Down) || (player.directionOfTravel == Direction.Up)) {
              player.turnDirection(Direction.Left)
            }
          })
        }
        case _ =>
      }
    } else if (InputManager.gameTickInputs.contains("GoRight_UP")) {
      state match {
        case GlobalControllerState.PLAYING => {
          ecs.system[Player].toList.flatMap(_._2).foreach(player => {
            if((player.directionOfTravel == Direction.Down) || (player.directionOfTravel == Direction.Up)) {
              player.turnDirection(Direction.Right)
            }
          })
        }
        case _ =>
      }
    } else if (InputManager.gameTickInputs.contains("GoUp_UP")) {
      state match {
        case GlobalControllerState.PLAYING => {
          ecs.system[Player].toList.flatMap(_._2).foreach(player => {
            if((player.directionOfTravel == Direction.Left) || (player.directionOfTravel == Direction.Right)) {
              player.turnDirection(Direction.Up)
            }
          })
        }
        case _ =>
      }
    } else if (InputManager.gameTickInputs.contains("GoDown_UP")) {
      state match {
        case GlobalControllerState.PLAYING => {
          ecs.system[Player].toList.flatMap(_._2).foreach(player => {
            if((player.directionOfTravel == Direction.Left) || (player.directionOfTravel == Direction.Right)) {
              player.turnDirection(Direction.Down)
            }
          })
        }
        case _ =>
      }
    }
  }
}
