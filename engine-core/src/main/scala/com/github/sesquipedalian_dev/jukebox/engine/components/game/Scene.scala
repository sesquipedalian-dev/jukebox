/**
  * Copyright 11/30/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components.game

import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.framework.Renderer
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D
import com.typesafe.scalalogging.LazyLogging

// component for a scene in the game's hierarchy.
// knows of the objects in that scene, and possibly some rendering for the scene's background / viewport
case class Scene(
  sceneObjects: List[UUIDIdType], // these should have component SceneObject
  backgroundImagePath: String, // where can the background be found in resources?
  size: List[SerializablePoint2D] // could be a polygon? let's assume simple rectangle bounds 4 now
)

case class SceneRenderer(
) extends Renderer with LazyLogging {
  var image: Option[Image] = None
  def render(entId: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit = {
    logger.trace("rendering scene 1 {}", entId)
    ecs.system[Scene].get(entId).foreach(scene => {
      logger.trace("rendering scene 2 {}", entId)
      if(image.isEmpty) {
        image = Some(new Image(scene.backgroundImagePath))
      }

      val offset = SceneController.viewport.head
      logger.trace("rendering scene 3 {} {} {}", entId, offset)
      gc.drawImage(image.get, -offset.x, -offset.y)
    })
  }
}
