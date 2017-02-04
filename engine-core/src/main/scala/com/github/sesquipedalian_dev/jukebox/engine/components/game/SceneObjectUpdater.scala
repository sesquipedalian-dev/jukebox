/**
  * Copyright 12/6/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components.game

import javafx.scene.input.MouseButton

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.{InputManager, UUIDIdType}
import com.github.sesquipedalian_dev.jukebox.engine.components.framework.Updater
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.typesafe.scalalogging.LazyLogging

case class SceneObjectUpdater(
) extends Updater with LazyLogging {
  override def update(eid: EntityIdType)(implicit ecs: ECS[UUIDIdType]): Unit = {
    ecs.system[SceneObject].get(eid).foreach(sceneObject => {
      InputManager.gameTickInputs.foreach {
        case "HighlightObjects_DOWN" => sceneObject.isHighlighting = true
        case "HighlightObjects_UP" => sceneObject.isHighlighting = false
        case _ =>
      }

      val thisObjectUnderMouse = InputManager.objectUnderMouse(ecs).find(_.eid == eid)
      thisObjectUnderMouse match {
        case Some(mouseStatus) => {
          sceneObject.isMousedOver = true
          if (mouseStatus.buttonStatus.exists(mbs => mbs.id == MouseButton.PRIMARY && mbs.up)) {
            sceneObject.isHighlighting = !sceneObject.isHighlighting
          }
        }
        case _ => {
          sceneObject.isMousedOver = false
        }
      }
    })
  }
}
