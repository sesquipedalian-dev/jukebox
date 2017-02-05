/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.modules

import javafx.event.{ActionEvent, EventHandler}

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.components.ComponentModule
import com.github.sesquipedalian_dev.jukebox.engine.{GameLoop, Main, UUIDIdType, components}
import com.github.sesquipedalian_dev.util.scalafx.MenuLookup

import scalafx.scene.control.MenuItem

/**
  * Maintain state of loaded modules
  */
object ModuleController {
  // name / id of the currently loaded module (or none if not loaded yet)
  var currentModule: Option[String] = None

  def loadModule(name: String, gameLoop: GameLoop): Unit = {
    // load module from class path
    val moduleClassName = "com.github.sesquipedalian_dev.jukebox.module." + name.toLowerCase() + "." + name + "Module"
    val moduleClass = Class.forName(moduleClassName)
    val moduleInstance = moduleClass.newInstance()

    // reload ECS
    components.addComponent(moduleInstance.asInstanceOf[ComponentModule])
    val (cb, newSystems) = components.makeNewECSSystems()
    val newEcs = ECS[UUIDIdType](newSystems:_*)
    gameLoop.ecs = newEcs
    cb()

    // call module init
    val method = moduleClass.getDeclaredMethod("onLoad")
    method.invoke(moduleInstance)

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

  def apply()(implicit gameLoop: GameLoop): Unit = {
    // TODO some way to load modules
    // TODO check for jars in a specific place or something...?
    val foundModules: List[String] = List("Asteroids")

    // hook up module loading to UI
    MenuLookup.topLevelLookup(Main.stage.scene(), "modulesMenu").foreach(menu => {
      val menuItems = foundModules.map(module => {
        moduleMap += (module -> "ANY")
        val menuItem = new MenuItem(module){
        }
        menuItem.onAction = new EventHandler[ActionEvent](){
          override def handle(event: ActionEvent): Unit = {
            loadModule(module, gameLoop)
          }
        }
        menuItem
      })
      menuItems.foreach(mi => menu.getItems.add(mi))
    })
  }
}
