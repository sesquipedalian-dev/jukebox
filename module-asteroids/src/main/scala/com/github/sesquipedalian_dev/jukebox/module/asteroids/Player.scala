/**
  * Copyright 2/6/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.{CANVAS_HEIGHT, CANVAS_WIDTH, UUIDIdType}
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Updater
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.GameLoopModule._
import com.github.sesquipedalian_dev.util.config.ConfigSetting
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

/*
 * Data struct for the player
 */
case class Player(
  var rotationRadians: Double,
  var livesRemaining: Int,
  var score: Int,
  var playingDeathAnim: Int = 0, // frames that we've been playing death 'anim',
  var pointsSinceExtraLife: Int = 0
) extends LazyLogging
{
  // player has fixed position
  def position: SerializablePoint2D = {
    SerializablePoint2D(CANVAS_WIDTH() / 2, CANVAS_HEIGHT() / 2)
  }

  def hitByObject(pid: UUIDIdType#EntityId, ecs: ECS[UUIDIdType]): Unit = {
    logger.info("hitByObject, dropping lives remaining {" + livesRemaining + "}")
    livesRemaining -= 1
    if(livesRemaining > 0) {
      // give us some invincibility frames and do some sort of blink effect
      playingDeathAnim = DEATH_ANIM_DURATION_FRAMES()
    } else {
      // do game over
      ecs.system[Updater].collect({
        case (k, v) => v.collect {
          case controller: AsteroidsGlobalController => controller
        }
      }).flatten.foreach(controller => {
        controller.state = GlobalControllerState.DIED
      })
    }
  }

  def addScore(s: Int): Unit = {
    score += s
    pointsSinceExtraLife += s
    if(pointsSinceExtraLife >= POINTS_FOR_EXTRA_LIFE()) {
      livesRemaining += 1
      pointsSinceExtraLife = pointsSinceExtraLife - POINTS_FOR_EXTRA_LIFE()
    }
  }
}
