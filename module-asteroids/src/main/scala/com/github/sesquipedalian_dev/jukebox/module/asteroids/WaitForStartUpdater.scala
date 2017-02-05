/**
  * Copyright 2/5/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.{InputManager, UUIDIdType}
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Updater

case class WaitForStartUpdater() extends Updater {
  override def update(eid: EntityIdType)(implicit ecs: ECS[UUIDIdType]): Unit = {
    if(InputManager.gameTickInputs.contains("Shoot_DOWN")) {
      if (AsteroidsModule.instance.startRenderer.nonEmpty) {
        ecs -= AsteroidsModule.instance.startRenderer.get
        ecs -= eid
        AsteroidsModule.instance.startRenderer = None

        // testing
        AsteroidsModule.instance.spawnAsteroid()
      }
    }
  }
}
