/**
  * Copyright 2/6/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import com.github.sesquipedalian_dev.util.config.ConfigSetting
import com.typesafe.config.Config

case object DEATH_ANIM_DURATION_FRAMES extends ConfigSetting[Int] {
  override def defaultValue: Int = 300
  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}
/*
 * Data struct for the player
 */
case class Player(
  rotationRadians: Double,
  livesRemaining: Int,
  score: Int,
  var playingDeathAnim: Long // milliseconds that we've been playing death 'anim'
) {

  def hitByBullet(): Unit = {
    if(livesRemaining > 0) {
      playingDeathAnim = DEATH_ANIM_DURATION_FRAMES()
    } else {
      // do game over
    }
  }
}
