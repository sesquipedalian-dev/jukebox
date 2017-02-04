/**
  * Copyright 11/24/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.ui

import javafx.scene.canvas.GraphicsContext

import com.github.sesquipedalian_dev.jukebox.engine.{GameLoop, Main}
import com.github.sesquipedalian_dev.util.config.LoadableBooleanConfigSetting
import com.github.sesquipedalian_dev.util.scalafx.BoolConfigSettingWithUI

import scalafx.beans.property.DoubleProperty
import scalafx.event.subscriptions.Subscription

case object FPSCounterVisible extends LoadableBooleanConfigSetting with BoolConfigSettingWithUI {
  override def configFileName: String = "jukebox.ui.showfps"
  override def defaultValue: Boolean = false
  override def userSetting: Boolean = true
}

case object TPSCounterVisible extends LoadableBooleanConfigSetting with BoolConfigSettingWithUI {
  override def configFileName: String = "jukebox.ui.showtps"
  override def defaultValue: Boolean = false
  override def userSetting: Boolean = true
}

case class FpsStruct(
  measureMillis: Long, // timestamp that the measurement started
  frames: Int // count of frames during this measurement
) {
  def update(): FpsStruct = this.copy(frames = frames + 1)
}

// manage javafx observable properties for game tick rate and fps
case class FpsCounter()(implicit gameLoop: GameLoop) {
  var gameTicksPerSecond = FpsStruct(0L, 0)
  val gameTicksPerSecondProp = DoubleProperty(0.0)
  var gameTicksPerSecondSubscription: Option[Subscription] = None
  var framesPerSecond = FpsStruct(0L, 0)
  val framesPerSecondProp = DoubleProperty(0.0)
  var framesPerSecondSubscription: Option[Subscription] = None

  // hook up our properties to their text display in the scene
  Main.stage.scene().lookup("#tpsText") match {
    case x if x == null =>
    case textNode: javafx.scene.text.Text => {
      TPSCounterVisible.onChange(newValue => {
        textNode.setVisible(newValue.getOrElse(false))
      })
      textNode.setVisible(TPSCounterVisible())
      gameTicksPerSecondSubscription = Some(gameTicksPerSecondProp.onChange((v, oldV, newV) => {
        textNode.setText("TPS: " + newV.toString)
      }))
    }
  }

  Main.stage.scene().lookup("#fpsText") match {
    case x if x == null =>
    case textNode: javafx.scene.text.Text => {
      FPSCounterVisible.onChange(newValue => {
        textNode.setVisible(newValue.getOrElse(false))
      })
      textNode.setVisible(FPSCounterVisible())
      framesPerSecondSubscription = Some(framesPerSecondProp.onChange((v, oldV, newV) => {
        textNode.setText("FPS: " + newV.toString)
      }))
    }
  }

  def update(): Unit = {
    // manage the tps measurement
    val now = System.currentTimeMillis()
    if (gameTicksPerSecond.measureMillis + 1000 < now) {
      gameTicksPerSecondProp() = gameTicksPerSecond.frames
      gameTicksPerSecond = FpsStruct(now, 0)
    }
    gameTicksPerSecond = gameTicksPerSecond.update()
  }
  gameLoop.onPreUpdate((ecs) => update(), -1001)

  def render(gc: GraphicsContext): Unit = {
    val now = System.currentTimeMillis()
    if(framesPerSecond.measureMillis + 1000 < now) {
      framesPerSecondProp() = framesPerSecond.frames
      framesPerSecond = FpsStruct(now, 0)
    }
    framesPerSecond = framesPerSecond.update()
  }
  gameLoop.onRender((gc, ecs) => render(gc), -1001)
}
