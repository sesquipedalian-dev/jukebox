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
  spriteImg: String
)
case object ASTEROID_PARAMS_MAP extends ConfigSetting[Map[String, AsteroidParams]] {
  override def defaultValue: Map[String, AsteroidParams] = List(
    AsteroidParams(
      size = "small",
      width = 50,
      height = 50,
      initialVelocityScale = 4.0,
      spriteImg = "img/smallAsteroid.jpg"
    ),
    AsteroidParams(
      size = "medium",
      width = 100,
      height = 100,
      initialVelocityScale = 2.0,
      spriteImg = "img/mediumAsteroid.jpg"
    ),
    AsteroidParams(
      size = "large",
      width = 200,
      height = 200,
      initialVelocityScale = 1.0,
      spriteImg = "img/largeAsteroid.jpg"
    )
  ).map(ap => (ap.size -> ap)).toMap

  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}