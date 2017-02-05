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
