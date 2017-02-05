/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.modules

/**
  * Maintain state of loaded modules
  */
object ModuleController {
  // name / id of the currently loaded module (or none if not loaded yet)
  var currentModule: Option[String] = None

  def loadModule(name: String): Unit = {
    // TODO load module
    currentModule = Some(name)
    moduleLoadCallbacks.foreach(_(name))
  }

  def availableModules(): List[String] = {
    // TODO check for jars in a specific place or something...?
    Nil
  }

  var moduleLoadCallbacks: List[(String) => Unit] = List()
  def onModuleLoad(cb: (String) => Unit) = {
    moduleLoadCallbacks :+= cb
  }
}
