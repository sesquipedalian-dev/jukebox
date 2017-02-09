/**
  * Created by Scott on 2/9/2017.
  */
package com.github.sesquipedalian_dev.jukebox.module.snake

import com.github.sesquipedalian_dev.util.config.ConfigSetting
import com.typesafe.config.Config

case object PIXEL_SIZE extends ConfigSetting[Int] {
  override def defaultValue: Int = 32
  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}

case object CANVAS_PIXELS_HEIGHT extends ConfigSetting[Int] {
  override def defaultValue: Int = 28
  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}

case object CANVAS_PIXELS_WIDTH extends ConfigSetting[Int] {
  override def defaultValue: Int = 50
  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}