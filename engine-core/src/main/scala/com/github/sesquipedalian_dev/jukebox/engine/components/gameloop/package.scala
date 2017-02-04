/**
  * Copyright 11/30/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components

import com.github.gigurra.scalego.serialization.KnownSubTypes
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType

import scala.collection.mutable

package object gameloop {
  val knownSubtypes: KnownSubTypes = KnownSubTypes.empty +
    ("renderer" -> classOf[Renderer]) +
    ("updater" -> classOf[Updater])

  /**
    * scalego systems for the various components we define in this package
    */
  implicit var rendererSystem =
    new com.github.gigurra.scalego.core.System[Renderer, UUIDIdType]("renderer", mutable.HashMap())
  implicit var updaterSystem =
    new com.github.gigurra.scalego.core.System[Updater, UUIDIdType]("updater", mutable.HashMap())

  def blankSystemsSet: (() => Unit, List[com.github.gigurra.scalego.core.System[_, UUIDIdType]]) = {
    val newRendererSystem =
      new com.github.gigurra.scalego.core.System[Renderer, UUIDIdType]("renderer", mutable.HashMap())
    val newUpdaterSystem =
      new com.github.gigurra.scalego.core.System[Updater, UUIDIdType]("updater", mutable.HashMap())
    val cb = () => {
      rendererSystem = newRendererSystem
      updaterSystem = newUpdaterSystem
    }

    val lst =
      newRendererSystem ::
      newUpdaterSystem ::
      Nil
    (cb, lst)
  }
}
