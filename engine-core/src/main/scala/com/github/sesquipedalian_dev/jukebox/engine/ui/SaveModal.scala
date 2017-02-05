/**
  * Copyright 11/27/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.ui

import java.io.IOException
import javafx.event.{ActionEvent, EventHandler}

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.{GameLoop, Main, UUIDIdType}
import com.github.sesquipedalian_dev.jukebox.engine.components._
import com.github.sesquipedalian_dev.jukebox.engine.Main
import com.github.sesquipedalian_dev.jukebox.engine.modules.ModuleController
import com.github.sesquipedalian_dev.util.config.ConfigSettings
import com.github.sesquipedalian_dev.util.ecs.{GameSave, USER_SAVES_LOC}
import com.github.sesquipedalian_dev.util.scalafx.{MenuLookup, SimpleModalDialogs}

import scalafx.geometry.Rectangle2D
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter
import com.github.sesquipedalian_dev.jukebox.engine.ui.I18nManager._

case class SaveModal()(implicit val ecs: ECS[UUIDIdType], gameLoop: GameLoop) {
  val UI_ICONS_FILE = "/fxml/ui-icons-222222-256x240.png"
  val UI_SETTINGS_ICON_BOUNDS = new Rectangle2D(110.0, 116.0, 10.0, 10.0)

  // find the menu item that should load up our modal
  val saveMenu = MenuLookup.oneLevelLookup(Main.stage.scene(), "fileMenu", "saveMenu")

  saveMenu.foreach(menu => {
    menu.setDisable(true)
    ModuleController.onModuleLoad((moduleName) => menu.setDisable(false))
    menu.setOnAction(new EventHandler[ActionEvent] {
      override def handle(event: ActionEvent) = {
        /*************************************
          * when save menu clicked, create dialog to save
          * adapted from http://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
          */
        try {
          val fileChooser = new FileChooser() {
            title = "Save Game"
            initialDirectory = GameSave.getSavesDir()
            extensionFilters.add(new ExtensionFilter(L("modal.save.savefiletype"), "*.json"))
          }

          // when done configuring, show dialog
          gameLoop.pause()
          val chosenSaveFile = fileChooser.showSaveDialog(Main.stage)
          gameLoop.unpause()

          if(chosenSaveFile != null) { // it's null if user canceled dialog
            if(chosenSaveFile.getParent() != USER_SAVES_LOC.getValue) {
              USER_SAVES_LOC.value = Some(chosenSaveFile.getParent())
              ConfigSettings.saveUserConfig()
            }

            GameSave.saveGame(ecs, chosenSaveFile.getName, ModuleController.currentModule.getOrElse("UnknownModule"), shouldOverwrite = true) // FileChooser dialog already confirmed overwrite
          }
        } catch {
          case x: IOException => {
            SimpleModalDialogs.showException(L("exception.save.ioerror"), x)
          }
          case y: Exception => {
            SimpleModalDialogs.showException(L("exception.save.unknownerror"), y)
          }
        }
      }
    })
  })


}
