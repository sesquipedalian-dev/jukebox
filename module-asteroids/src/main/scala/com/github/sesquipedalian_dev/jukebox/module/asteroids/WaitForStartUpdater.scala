/**
  * Copyright 2/5/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.{InputManager, UUIDIdType}
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Updater
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.SceneObject
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.ObjectsModule._
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D

case class WaitForStartUpdater() extends Updater {
  override def update(eid: EntityIdType)(implicit ecs: ECS[UUIDIdType]): Unit = {
    if(InputManager.gameTickInputs.contains("Shoot_DOWN")) {
      if (AsteroidsModule.instance.startRenderer.nonEmpty) {
        ecs -= AsteroidsModule.instance.startRenderer.get
        ecs -= eid
        AsteroidsModule.instance.startRenderer = None

        // testing
        val asteroidId = AsteroidsModule.instance.spawnAsteroid()
        val asteroidSceneObject = ecs.system[SceneObject].get(asteroidId)
        AsteroidsModule.instance.spawnBullet(asteroidSceneObject.get, SerializablePoint2D(1.0, 0))
      }
    }
  }
}
