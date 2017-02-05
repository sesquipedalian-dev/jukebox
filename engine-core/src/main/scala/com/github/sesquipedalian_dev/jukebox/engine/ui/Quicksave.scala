/**
  * Copyright 11/30/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.ui

import java.io.File
import javafx.event.{ActionEvent, EventHandler}

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.{GameLoop, Main, UUIDIdType}
import com.github.sesquipedalian_dev.jukebox.engine.components._
import com.github.sesquipedalian_dev.jukebox.engine.modules.ModuleController
import com.github.sesquipedalian_dev.jukebox.engine.ui.I18nManager.L
import com.github.sesquipedalian_dev.jukebox.engine.{Main, components}
import com.github.sesquipedalian_dev.util.config.LoadableStringConfigSetting
import com.github.sesquipedalian_dev.util.ecs.{GameSave, USER_SAVES_LOC}
import com.github.sesquipedalian_dev.util.scalafx.{MenuLookup, SimpleModalDialogs, StringConfigSettingWithUI}
import com.typesafe.scalalogging.LazyLogging

case object QUICKSAVE_FILENAME extends LoadableStringConfigSetting with StringConfigSettingWithUI {
  override val configFileName: String = "jukebox.save.quicksave.filename"
  override val defaultValue: String = "quicksave"
  override val userSetting: Boolean = true
}

// hooks up quicksave / quickload menu items
case class Quicksave()(implicit ecs: ECS[UUIDIdType], gameLoop: GameLoop) extends LazyLogging {
  // lookup save menu item and hook up our handler
  val saveMenu = MenuLookup.oneLevelLookup(Main.stage.scene(), "fileMenu", "quickSave")
  saveMenu.foreach(menu => {
    menu.setDisable(true)
    ModuleController.onModuleLoad((moduleName) => menu.setDisable(false))
    menu.setOnAction(new EventHandler[ActionEvent]() {
      override def handle(event: ActionEvent): Unit = save()
    })
  })

  // hook up our quick load handler to the menu item
  val loadMenu = MenuLookup.oneLevelLookup(Main.stage.scene(), "fileMenu", "quickLoad")
  loadMenu.foreach(menu => {
    menu.setDisable(true)
    ModuleController.onModuleLoad((moduleName) => menu.setDisable(false))
    menu.setOnAction(new EventHandler[ActionEvent]() {
      override def handle(event: ActionEvent): Unit = load()
    })
  })

  // save to the quicksave file when requested
  def save() {
    try {
      gameLoop.pause()
      GameSave.saveGame(ecs, getQuicksaveFilename, ModuleController.currentModule.getOrElse("UnknownModule"), shouldOverwrite = true)
      loadMenu.foreach(_.setDisable(false)) // if we've successfully saved, we can re-enable the quickload menu
    } catch {
      case x: Exception => {
        SimpleModalDialogs.showException("Error quicksaving!", x)
      }
    } finally {
      gameLoop.unpause()
    }
  }

  // when quick save filename changes, we have to check if we have a file to load
  QUICKSAVE_FILENAME.onChange(possibleVal => {
    possibleVal.foreach(s => {
      if(!checkExists) {
        loadMenu.foreach(_.setDisable(true))
      }
    })
  })
  USER_SAVES_LOC.onChange(possibleVal => {
    possibleVal.foreach(s => {
      if(!checkExists) {
        loadMenu.foreach(_.setDisable(true))
      }
    })
  })

  // when requested, load the quicksave file
  def load(): Unit = {
    try {
      gameLoop.pause()
      val (cb, newSystems) = components.makeNewECSSystems
      val (moduleName: String, newEcs: ECS[UUIDIdType]) = GameSave.loadGame[UUIDIdType](getQuicksaveFilename, newSystems)
      if(moduleName != ModuleController.currentModule.getOrElse("Unknown")) {
        SimpleModalDialogs.showException(L("exception.load.ioerror"), new Exception())
      } else {
        gameLoop.ecs = newEcs
        cb()
      }
    } catch {
      case y: Exception => {
        SimpleModalDialogs.showException("Unknown Error while loading file!", y)
      }
    } finally {
      gameLoop.unpause()
    }
  }

  // verify the quicksave file exists (used to enable / disable the quickload menu item)
  def checkExists: Boolean = {
    logger.trace("checking if quicksave file exists {} {} {}", USER_SAVES_LOC.getValue, getQuicksaveFilename, ".json")
    new File(USER_SAVES_LOC.getValue + "/" + getQuicksaveFilename).exists()
  }

  def getQuicksaveFilename: String = {
    QUICKSAVE_FILENAME.getValue + "_" + "CURRENT MODULE NAME" + ".json"
  }
}
