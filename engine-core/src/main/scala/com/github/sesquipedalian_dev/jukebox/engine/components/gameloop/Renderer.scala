/**
  * Copyright 11/24/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components.gameloop

import javafx.scene.canvas.GraphicsContext

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components._

// components that game loop calls to render
trait Renderer {
  def renderOrder(ecs: ECS[UUIDIdType], eid: UUIDIdType#EntityId): Int
  def render(eid: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit
}
