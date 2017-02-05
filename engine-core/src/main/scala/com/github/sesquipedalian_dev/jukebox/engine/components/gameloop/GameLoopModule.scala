/**
  * Copyright 11/30/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components.gameloop

import com.github.gigurra.scalego.core.System
import com.github.gigurra.scalego.serialization.KnownSubTypes
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.ComponentModule

import scala.collection.mutable

object GameLoopModule extends ComponentModule {
  val subtypes: KnownSubTypes = KnownSubTypes.empty +
    ("renderer" -> classOf[Renderer]) +
    ("updater" -> classOf[Updater])

  /**
    * scalego systems for the various components we define in this package
    */
  implicit var rendererSystem =
    new com.github.gigurra.scalego.core.System[Renderer, UUIDIdType]("renderer", mutable.HashMap())
  implicit var updaterSystem =
    new com.github.gigurra.scalego.core.System[Updater, UUIDIdType]("updater", mutable.HashMap())

  override def makeSystems(): List[System[_, UUIDIdType]] = {
    (new com.github.gigurra.scalego.core.System[Renderer, UUIDIdType]("renderer", mutable.HashMap())) ::
    (new com.github.gigurra.scalego.core.System[Updater, UUIDIdType]("updater", mutable.HashMap())) ::
    Nil
  }

  override def systemsMadeCallback(systemsMade: List[System[_, UUIDIdType]]): Unit = {
    rendererSystem = systemsMade.head.asInstanceOf[com.github.gigurra.scalego.core.System[Renderer, UUIDIdType]]
    updaterSystem = systemsMade.drop(1).head.asInstanceOf[com.github.gigurra.scalego.core.System[Updater, UUIDIdType]]
  }
}
