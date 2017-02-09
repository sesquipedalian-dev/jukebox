/**
  * Created by Scott on 2/9/2017.
  */
package com.github.sesquipedalian_dev.jukebox.module.snake

import javafx.scene.canvas.GraphicsContext
import javafx.scene.text.Font

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Renderer
import com.github.sesquipedalian_dev.jukebox.module.snake.SnakeModule._
import com.typesafe.scalalogging.LazyLogging

import scalafx.scene.paint.Color

case class PlayerRenderer(
) extends Renderer
  with LazyLogging
{
  override def renderOrder(ecs: ECS[UUIDIdType], eid: EntityIdType): Int = 5

  override def render(eid: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit = {
    ecs.system[Player].getOrElse(eid, Nil).foreach(player => {
      gc.setFill(Color.Blue)
      gc.setStroke(Color.Blue)
      gc.setLineWidth(2)

      player.segments.foreach(segment => {
        val startX = segment.x * PIXEL_SIZE()
        val startY = segment.y * PIXEL_SIZE()
        logger.trace("rendering player segment {}", segment)
        gc.fillRect(startX - 6, startY - 3, PIXEL_SIZE() - 6, PIXEL_SIZE()-6)
      })
    })
  }
}
