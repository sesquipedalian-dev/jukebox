/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.modules

import javafx.event.{ActionEvent, EventHandler}

import com.github.sesquipedalian_dev.jukebox.engine.Main
import com.github.sesquipedalian_dev.util.scalafx.MenuLookup

import scalafx.scene.control.MenuItem

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
    moduleMap.keys.toList
  }

  var moduleLoadCallbacks: List[(String) => Unit] = List()
  def onModuleLoad(cb: (String) => Unit) = {
    moduleLoadCallbacks :+= cb
  }

  private var moduleMap: Map[String, Any] = Map() // TODO need some object to store module details!? for loading!?

  def apply(): Unit = {
    // TODO some way to load modules
    // TODO check for jars in a specific place or something...?
    val foundModules: List[String] = List("Modulo1")

    // hook up module loading to UI
    MenuLookup.topLevelLookup(Main.stage.scene(), "modulesMenu").foreach(menu => {
      val menuItems = foundModules.map(module => {
        moduleMap += (module -> "ANY")
        val menuItem = new MenuItem(module){
        }
        menuItem.onAction = new EventHandler[ActionEvent](){
          override def handle(event: ActionEvent): Unit = {
            loadModule(module)
          }
        }
        menuItem
      })
      menuItems.foreach(mi => menu.getItems.add(mi))
    })
  }
}
