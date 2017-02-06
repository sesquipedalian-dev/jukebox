/**
  * Copyright 2/5/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import javafx.scene.canvas.GraphicsContext
import javafx.scene.text.Font

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.{Renderer, Updater}
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.GameLoopModule._
import com.typesafe.scalalogging.LazyLogging

import scalafx.scene.paint.Color

case class MessageTextRenderer()
  extends Renderer
  with LazyLogging
{
  def renderOrder(ecs: ECS[UUIDIdType], eid: UUIDIdType#EntityId): Int = 5 // text layer

  override def render(eid: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit = {
    ecs.system[Updater].getOrElse(eid, Nil).collect({case x: AsteroidsGlobalController => x}).foreach(globalController => {
      val text = globalController.state match {
        case READY_TO_START => {
          Some("***Press Enter to Start***")
        }
        case PLAYING => {
          None
        }
        case DIED => {
          Some("***Game Over! Enter to re-start")
        }
      }
      logger.trace("rendering some text? {}", text)
      text.foreach(t => {
        gc.setLineWidth(2)
        gc.setStroke(Color.White)
        gc.setFont(new Font(gc.getFont.getName, 40))
        gc.strokeText(t, 500, 400)
      })
    })
  }
}