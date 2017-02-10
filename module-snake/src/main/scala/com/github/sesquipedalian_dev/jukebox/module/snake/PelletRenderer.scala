/**
  * Created by Scott on 2/9/2017.
  */
package com.github.sesquipedalian_dev.jukebox.module.snake

import javafx.scene.canvas.GraphicsContext

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Renderer
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D

import scalafx.scene.paint.Color

case class PelletRenderer(
  position: SerializablePoint2D
) extends Renderer {
  override def renderOrder(ecs: ECS[UUIDIdType], eid: EntityIdType): Int = 0
  override def render(eid: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit = {
    gc.setFill(Color.Yellow)
    gc.setStroke(Color.Yellow)
    gc.setLineWidth(2)

    val startX = position.x * PIXEL_SIZE()
    val startY = position.y * PIXEL_SIZE()
    gc.fillRect(startX - 6, startY - 3, PIXEL_SIZE() - 6, PIXEL_SIZE()-6)
  }
}
