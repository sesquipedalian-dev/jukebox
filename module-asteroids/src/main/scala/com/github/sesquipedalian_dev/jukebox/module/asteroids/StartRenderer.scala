/**
  * Copyright 2/5/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import javafx.scene.canvas.GraphicsContext
import javafx.scene.text.Font

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Renderer

import scalafx.scene.paint.Color

case class StartRenderer() extends Renderer {
  def renderOrder(ecs: ECS[UUIDIdType], eid: UUIDIdType#EntityId): Int = 5 // text layer

  override def render(eid: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit = {
    gc.setLineWidth(2)
    gc.setStroke(Color.White)
    gc.setFont(new Font(gc.getFont.getName, 40))
    gc.strokeText("***Press Enter to Start***", 500, 400)
  }
}