/**
  * Copyright 11/24/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components.gameloop

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components._

// components that handle game loop updates
trait Updater {
  def update(eid: EntityIdType)(implicit ecs: ECS[UUIDIdType]): Unit
}
