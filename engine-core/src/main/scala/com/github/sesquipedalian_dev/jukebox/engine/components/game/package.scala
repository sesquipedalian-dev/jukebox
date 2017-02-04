/**
  * Copyright 11/30/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components

import com.github.gigurra.scalego.serialization.KnownSubTypes
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType

import scala.collection.mutable

package object game {
  val knownSubtypes: KnownSubTypes = KnownSubTypes.empty +
    ("scene-renderer" -> classOf[SceneRenderer]) +
    ("scene-object-renderer" -> classOf[SceneObjectRenderer]) +
    ("scene-object-updater" -> classOf[SceneObjectUpdater])

  /**
    * scalego systems for the various components we define in this package
    */
  implicit var sceneSystem =
    new com.github.gigurra.scalego.core.System[Scene, UUIDIdType]("scene", mutable.HashMap())
  implicit var sceneObjectSystem =
    new com.github.gigurra.scalego.core.System[SceneObject, UUIDIdType]("sceneObject", mutable.HashMap())

  def blankSystemsSet: (() => Unit, List[com.github.gigurra.scalego.core.System[_, UUIDIdType]]) = {
    val newSceneSystem =
      new com.github.gigurra.scalego.core.System[Scene, UUIDIdType]("scene", mutable.HashMap())
    val newSceneObjectSystem =
      new com.github.gigurra.scalego.core.System[SceneObject, UUIDIdType]("sceneObject", mutable.HashMap())

    val cb = () => {
      sceneSystem = newSceneSystem
      sceneObjectSystem = newSceneObjectSystem
    }

    val lst =
      newSceneSystem ::
      newSceneObjectSystem ::
      Nil
    (cb, lst)
  }


  /*
   * This section is like prefabs; creatures particular kinds of entities
   * that we know have a shared set of components they want to use.
   */
}
