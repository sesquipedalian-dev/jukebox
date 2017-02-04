/**
  * Copyright 11/30/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components.objects

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine._
import com.github.sesquipedalian_dev.util.config.{ConfigSetting, LoadableDoubleConfigSetting}
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scalafx.geometry.Point2D

// TODO - a linear curve for scroll speed acceleration looks pretty janky.
// also, there's some tearing-like behaviour going on where when we get to the right edge, it can't quite make it
// to the edge when the scroll speed is on full?

case object INPUT_SCROLL_SPEED extends LoadableDoubleConfigSetting {
  override def configFileName: String = "tycoon.input.scrollSpeed"
  override def defaultValue: Double = 10.0
  override def userSetting: Boolean = true
}

case object SCROLL_SPEED_MAX extends ConfigSetting[Double] {
  override def defaultValue: Double = 50.0
  override def populateFromConfig(newConfig: Config): Unit = {} // not populated from config at this time
}

// ECS component that holds a reference to all the scenes in the game, as well as the 'active' one
// TODO we might want this to be part of the ECS again for the convenience of doing save games
case object SceneController extends LazyLogging {
  var activeScene: Option[UUIDIdType#EntityId] = None // index in scenes list,
  var viewport: List[SerializablePoint2D] = List(
    SerializablePoint2D(0,0),
    SerializablePoint2D(CANVAS_WIDTH.getValue, CANVAS_HEIGHT.getValue)
  ) // currently visible part of current scene; should contain bottomLeft and topRight
  var xScrollSpeed: Double = 0.0

  def activeScene(ecs: ECS[UUIDIdType]): Option[Scene] = {
    activeScene.flatMap(sid => {
      ecs.system[Scene].get(sid)
    })
  }

  def sceneById(eid: UUIDIdType#EntityId, ecs: ECS[UUIDIdType]): Option[Scene] = {
    ecs.system[Scene].get(eid)
  }

  def update(ecs: ECS[UUIDIdType]): Unit = {
    InputManager.gameTickInputs.foreach {
      case "LeftScroll_DOWN" => xScrollSpeed = math.max(-SCROLL_SPEED_MAX.getValue, xScrollSpeed - INPUT_SCROLL_SPEED.getValue)
      case "LeftScroll_UP" => xScrollSpeed = 0.0
      case "RightScroll_DOWN" => xScrollSpeed = math.min(SCROLL_SPEED_MAX.getValue, xScrollSpeed + INPUT_SCROLL_SPEED.getValue)
      case "RightScroll_UP" => xScrollSpeed = 0.0
      case _ =>
    }
    logger.trace(s"SceneController updating my viewport? ${xScrollSpeed} ${InputManager.gameTickInputs}")
    activeScene.flatMap(sceneId => ecs.system[Scene].get(sceneId)).foreach(activeScene => {
      if(xScrollSpeed > .1 || xScrollSpeed < -.1) {
        val viewportRight = viewport.last
        val viewportLeft = viewport.head
        viewport = viewport.map(p => {
          val leftBounds = activeScene.size.head
          val rightBounds = activeScene.size.last
          p.copy(x = math.max(leftBounds.x, math.min(rightBounds.x - viewportRight.x, p.x + xScrollSpeed)))
        })

        // adjust the stored mouse pointer based on our viewport scolling
        InputManager.currentMousePointer = InputManager.currentMousePointer.map(point => {
          val newX = point.x - viewportLeft.x + viewport.head.x
          val newY = point.y - viewportLeft.y + viewport.head.y
          new Point2D(newX, newY)
        })
      }
    })
  }

  // initialize
  def apply()(implicit gameLoop: GameLoop): Unit = {
    gameLoop.onPreUpdate(update, -500)
  }
}