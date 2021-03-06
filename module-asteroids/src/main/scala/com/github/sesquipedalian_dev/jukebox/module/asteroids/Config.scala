/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import com.github.sesquipedalian_dev.util.config.{ConfigSetting, LoadableDoubleConfigSetting}
import com.typesafe.config.Config


case object STARTING_PLAYER_LIVES extends ConfigSetting[Int] {
  override def defaultValue: Int = 3
  override def userSetting: Boolean = false

  override def populateFromConfig(newConfig: Config): Unit = {} // nop
}

case class AsteroidParams(
  size: String,
  width: Double,
  height: Double,
  initialVelocityScale: Double,
  spriteImg: String,
  scoreForDestroy: Int
)

case object ASTEROID_PARAMS_MAP extends ConfigSetting[Map[String, AsteroidParams]] {
  override def defaultValue: Map[String, AsteroidParams] = List(
    AsteroidParams(
      size = "small",
      width = 50,
      height = 50,
      initialVelocityScale = 4.0,
      spriteImg = "img/smallAsteroid.gif",
      scoreForDestroy = 1000
    ),
    AsteroidParams(
      size = "medium",
      width = 100,
      height = 100,
      initialVelocityScale = 2.0,
      spriteImg = "img/mediumAsteroid.gif",
      scoreForDestroy = 500
    ),
    AsteroidParams(
      size = "large",
      width = 200,
      height = 200,
      initialVelocityScale = 1.0,
      spriteImg = "img/largeAsteroid.gif",
      scoreForDestroy = 250
    )
  ).map(ap => (ap.size -> ap)).toMap

  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}

case object PLAYER_ROTATION_SPEED_RADIANS extends ConfigSetting[Double] {
  override def defaultValue: Double = Math.PI / 180 * 7.5
  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}

case object MIN_FRAMES_BETWEEN_BULLETS extends ConfigSetting[Int] {
  override def defaultValue: Int = 40 // one sec
  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}

case object DEATH_ANIM_DURATION_FRAMES extends ConfigSetting[Int] {
  override def defaultValue: Int = 120
  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}

case object POINTS_FOR_EXTRA_LIFE extends ConfigSetting[Int] {
  override def defaultValue: Int = 10000
  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}

case object INITIAL_ASTEROID_COUNT extends ConfigSetting[Int] {
  override def defaultValue: Int = 4
  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}

case object FRAMES_BETWEEN_ASTEROID_SPAWN extends ConfigSetting[Int] {
  override def defaultValue: Int = 1200
  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}