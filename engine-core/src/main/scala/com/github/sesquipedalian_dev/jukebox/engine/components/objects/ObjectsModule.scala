/**
  * Copyright 11/30/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components.objects

import com.github.gigurra.scalego.core.System
import com.github.gigurra.scalego.serialization.KnownSubTypes
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.ComponentModule

import scala.collection.mutable

object ObjectsModule extends ComponentModule {
  override val subtypes: KnownSubTypes = KnownSubTypes.empty +
    ("scene-renderer" -> classOf[SceneRenderer]) +
    ("scene-object-renderer" -> classOf[SceneObjectRenderer]) +
    ("scene-object-updater" -> classOf[SceneObjectUpdater])

  implicit var sceneSystem =
    new com.github.gigurra.scalego.core.System[Scene, UUIDIdType]("scene")
  implicit var sceneObjectSystem =
    new com.github.gigurra.scalego.core.System[SceneObject, UUIDIdType]("sceneObject")

  override def makeSystems(): List[System[_, UUIDIdType]] = {
    (new com.github.gigurra.scalego.core.System[Scene, UUIDIdType]("scene")) ::
    (new com.github.gigurra.scalego.core.System[SceneObject, UUIDIdType]("sceneObject")) ::
    Nil
  }

  override def systemsMadeCallback(systemsMade: List[System[_, UUIDIdType]]): Unit = {
    sceneSystem = systemsMade.drop(0).head.asInstanceOf[com.github.gigurra.scalego.core.System[Scene, UUIDIdType]]
    sceneObjectSystem = systemsMade.drop(1).head.asInstanceOf[com.github.gigurra.scalego.core.System[SceneObject, UUIDIdType]]
  }

  def onLoad(): Unit = {}
}
