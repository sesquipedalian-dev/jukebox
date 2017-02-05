/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import javafx.scene.canvas.GraphicsContext

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Renderer

import scalafx.scene.paint.Color

case class ScoreRenderer() extends Renderer {
  override def render(eid: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit = {
    gc.setLineWidth(2)
    gc.setStroke(Color.White)
    gc.strokeText(AsteroidsModule.instance.score.toString, 50, 50)
  }
}
