/**
  * Copyright 2/5/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.snake

import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Renderer
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D
import com.typesafe.scalalogging.LazyLogging

case class BackgroundRenderer() extends Renderer with LazyLogging {
  def renderOrder(ecs: ECS[UUIDIdType], eid: UUIDIdType#EntityId): Int = -1000 // background layer

  var image: Option[Image] = None
  override def render(eid: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit = {
    try {
      if (image.isEmpty) {
        val classLoader = getClass().getClassLoader
        val imageStream = classLoader.getResourceAsStream("img/snake_background.jpg")
        image = Some(new Image(imageStream))
      }

      val offset = new SerializablePoint2D(0, 0) // just in case we need to scroll
      gc.drawImage(image.get, -offset.x, -offset.y)
    } catch {
      case x: Throwable => {
        logger.error("Exception when loading image? {} {} {}", x, x.getMessage, x.getStackTrace)
      }
    }
  }
}
