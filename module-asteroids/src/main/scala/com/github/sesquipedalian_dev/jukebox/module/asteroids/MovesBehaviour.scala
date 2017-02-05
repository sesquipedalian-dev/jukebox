/**
  * Copyright 2/5/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Updater
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.ObjectsModule._
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.SceneObject
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D

case class MovesBehaviour(
  velocityVector: SerializablePoint2D
) extends Updater {
  override def update(eid: EntityIdType)(implicit ecs: ECS[UUIDIdType]): Unit = {
    ecs.system[SceneObject].get(eid).foreach(sceneObject => {
      val newPolygon = sceneObject.polygon.map(point => point + velocityVector)
      sceneObject.polygon = newPolygon
    })
  }
}
