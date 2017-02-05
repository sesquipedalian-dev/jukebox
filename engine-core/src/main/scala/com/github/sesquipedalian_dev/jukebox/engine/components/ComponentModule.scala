/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components

import com.github.gigurra.scalego.serialization.KnownSubTypes
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType

/**
  * Trait for a component that adds systems to the ECS.  The systems typically need to be an implicit var, so
  * the implementor will typically need a callback once the ECS is populated to switch its implicit var over
  * to the newly-created system.
  */
trait ComponentModule {
  /*
   * a module can also have things that are in the ECS as subtypes of a trait / class in the original ECS
   * (especially Updater, Renderer).  This interface lets the module make the ECS aware of these subtypes.
   */
  def subtypes: KnownSubTypes

  /*
   * creates a blank set of Systems this component is responsible for
   */
  def makeSystems(): List[com.github.gigurra.scalego.core.System[_, UUIDIdType]]

  /*
   * Called when the system is done populating, so the systems can be copied into our implicit vars.
   * This confirmation step is needed in case the ecs fails to load something
   */
  def systemsMadeCallback(systemsMade: List[com.github.gigurra.scalego.core.System[_, UUIDIdType]]): Unit

  /*
   * Called when the module is loaded dynamically, used for doing initial init.
   */
  def onLoad(): Unit
}
