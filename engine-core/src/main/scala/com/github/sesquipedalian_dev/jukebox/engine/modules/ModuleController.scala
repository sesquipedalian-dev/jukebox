/**
  * Copyright 2/4/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.modules

import java.io.File
import java.net.{URL, URLClassLoader}
import javafx.event.{ActionEvent, EventHandler}

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.components.ComponentModule
import com.github.sesquipedalian_dev.jukebox.engine.{GameLoop, Main, UUIDIdType, components}
import com.github.sesquipedalian_dev.util.ResourceLoader
import com.github.sesquipedalian_dev.util.scalafx.MenuLookup
import com.typesafe.scalalogging.LazyLogging

import scalafx.scene.control.MenuItem

/**
  * Maintain state of loaded modules
  */
object ModuleController extends LazyLogging {
  // name / id of the currently loaded module (or none if not loaded yet)
  var currentModule: Option[String] = None

  def loadModule(name: String, gameLoop: GameLoop): Unit = {
    // make new class loader that knows about our jar
    val myJarPath = ModuleController.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath
    val myModulePath = new File(new File(myJarPath).getParent + "/modules/module_" + name.toLowerCase +".jar")
    val urlArray = Array[URL](myModulePath.toURI.toURL)
    println("resource path for my URL {" + myModulePath.toURI.toURL + "}")
    val newClassLoader = new URLClassLoader(urlArray, this.getClass.getClassLoader)
    ResourceLoader.installResourceLoader(newClassLoader)

    // load module from class path
    val moduleClassName = "com.github.sesquipedalian_dev.jukebox.module." + name.toLowerCase() + "." + name + "Module"
    logger.info("Attempting to load module class from custom class loader {" + moduleClassName + "}")
    val moduleClass = newClassLoader.loadClass(moduleClassName)
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
    moduleMap
  }

  var moduleLoadCallbacks: List[(String) => Unit] = List()
  def onModuleLoad(cb: (String) => Unit) = {
    moduleLoadCallbacks :+= cb
  }

  private var moduleMap: List[String] = List()

  val moduleFileRegex = "module_(.*).jar".r
  def apply()(implicit gameLoop: GameLoop): Unit = {
    // check what modules are available in the directory
    val myJarPath = ModuleController.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath
    val myModulePath = new File(new File(myJarPath).getParent + "/modules/")
    val foundModules = if(myModulePath.isDirectory) {
      myModulePath.listFiles().map(_.getName).collect({
        case moduleFileRegex(moduleName) => moduleName.head.toUpper +: moduleName.drop(1)
      }).toList
    } else {
      Nil
    }

    // hook up module loading to UI
    MenuLookup.topLevelLookup(Main.stage.scene(), "modulesMenu").foreach(menu => {
      val menuItems = foundModules.map(module => {
        moduleMap :+= module
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
