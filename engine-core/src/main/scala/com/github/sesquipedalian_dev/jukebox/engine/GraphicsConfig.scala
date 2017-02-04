/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine

import com.github.sesquipedalian_dev.util.config.{ConfigSetting, LoadableDoubleConfigSetting}
import com.github.sesquipedalian_dev.util.scalafx.{ConfigSettingWithUI, ConfigSettingWithUIController}
import com.typesafe.config.Config

import scalafx.scene.control.ComboBox

case object WINDOW_WIDTH extends LoadableDoubleConfigSetting {
  override def configFileName: String = "tycoon.window.width"
  override def defaultValue: Double = 1600
  override def userSetting: Boolean = true
}

case object WINDOW_HEIGHT extends LoadableDoubleConfigSetting {
  override def configFileName: String = "tycoon.window.height"
  override def defaultValue: Double = 925
  override def userSetting: Boolean = true
}

case object CANVAS_WIDTH extends LoadableDoubleConfigSetting {
  override def configFileName: String = "tycoon.canvas.width"
  override def defaultValue: Double = 1600
}

case object CANVAS_HEIGHT extends LoadableDoubleConfigSetting {
  override def configFileName: String = "tycoon.canvas.height"
  override def defaultValue: Double = 900
}

// UI-only config setting that sets the window width and height with a unified UI
// and restricts the user to a selection of pre-canned resolutions.
case object SCREEN_RESOLUTION extends ConfigSettingWithUI with ConfigSetting[(Double, Double)] {
  val ALLOWED_RESOLUTIONS: List[(Double, Double)] = List(
    (720, 405),
    (1024, 576),
    (1280, 720),
    (1600, 900),
    (2048, 1152),
    (2560, 1440)
  )

  override def createUI(okButton: javafx.scene.Node): ConfigSettingWithUIController = {
    val current = ALLOWED_RESOLUTIONS.find(p => (p._1 == WINDOW_WIDTH.getValue) && (p._2 == WINDOW_HEIGHT.getValue))

    val cb = new ComboBox[(Double, Double)](ALLOWED_RESOLUTIONS) {
      value = current.getOrElse(ALLOWED_RESOLUTIONS.head)
      visibleRowCount = ALLOWED_RESOLUTIONS.size
    }

    new ConfigSettingWithUIController() {
      override def label: String = "tycoon.window.resolution"
      override def node: scalafx.scene.Node = cb
      override def onSave(): Boolean = {
        val selected = cb.value()
        var changed = false
        if(selected._1 != WINDOW_WIDTH.getValue) {
          WINDOW_WIDTH.value = Some(selected._1)
          changed = true
        }

        if(selected._2 != WINDOW_HEIGHT.getValue) {
          WINDOW_HEIGHT.value = Some(selected._2)
          changed = true
        }

        changed
      }
    }
  }

  // not a real config so these don't matter
  override def defaultValue: (Double, Double) = (0.0, 0.0)
  override def populateFromConfig(newConfig: Config): Unit = {}
}
