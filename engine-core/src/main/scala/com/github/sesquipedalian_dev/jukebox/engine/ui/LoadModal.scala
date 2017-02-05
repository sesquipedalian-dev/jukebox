/**
  * Copyright 11/28/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.ui

import java.io.IOException
import javafx.event.{ActionEvent, EventHandler}

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.{GameLoop, Main, UUIDIdType}
import com.github.sesquipedalian_dev.jukebox.engine.components._
import com.github.sesquipedalian_dev.jukebox.engine.modules.ModuleController
import com.github.sesquipedalian_dev.jukebox.engine.{Main, components}
import com.github.sesquipedalian_dev.util.ecs.GameSave
import com.github.sesquipedalian_dev.util.scalafx.{MenuLookup, SimpleModalDialogs}

import scalafx.geometry.Rectangle2D
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import com.github.sesquipedalian_dev.jukebox.engine.ui.I18nManager._

case class LoadModal()(implicit val ecs: ECS[UUIDIdType], gameLoop: GameLoop) {
  val UI_ICONS_FILE = "/fxml/ui-icons-222222-256x240.png"
  val UI_SETTINGS_ICON_BOUNDS = new Rectangle2D(110.0, 116.0, 10.0, 10.0)

  // find the menu item that should load up our modal
  val saveMenu = MenuLookup.oneLevelLookup(Main.stage.scene(), "fileMenu", "loadMenu")

  saveMenu.foreach(menu => {
    menu.setDisable(true)
    ModuleController.onModuleLoad((moduleName) => menu.setDisable(false))
    menu.setOnAction(new EventHandler[ActionEvent] {
      override def handle(event: ActionEvent) = {
        /*************************************
          * when load menu clicked, create dialog to load
          * adapted from http://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
          */
        gameLoop.pause()
        try {
          val fileChooser = new FileChooser() {
            title = "Load Saved Game"
            initialDirectory = GameSave.getSavesDir()
            extensionFilters.add(new ExtensionFilter(L("modal.save.savefiletype"), "*.json"))
          }

          // when done configuring, show dialog
          val chosenSaveFile = fileChooser.showOpenDialog(Main.stage)

          val (cb, newSystems) = components.makeNewECSSystems
          if(chosenSaveFile != null) { // it's null if user canceled dialog
            val (moduleName: String, newEcs: ECS[UUIDIdType]) = GameSave.loadGame[UUIDIdType](chosenSaveFile.getName, newSystems)
            if(moduleName != ModuleController.currentModule.getOrElse("Unknown")) {
              SimpleModalDialogs.showException(L("exception.load.ioerror"), new Exception())
            } else {
              gameLoop.ecs = newEcs
              cb()
            }
          }
        } catch {
          case x: IOException => {
            SimpleModalDialogs.showException(L("exception.load.ioerror"), x)
          }
          case y: Exception => {
            SimpleModalDialogs.showException(L("exception.load.unknownerror"), y)
          }
        } finally {
          gameLoop.unpause()
        }
      }
    })
  })


}
