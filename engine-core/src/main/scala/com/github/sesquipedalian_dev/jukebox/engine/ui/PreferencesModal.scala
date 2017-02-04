/**
  * Copyright 11/27/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.ui

import javafx.event.{ActionEvent, EventHandler}

import com.github.sesquipedalian_dev.jukebox.engine.{GameLoop, Main}
import com.github.sesquipedalian_dev.util.config.ConfigSettings
import com.github.sesquipedalian_dev.util.scalafx.{ConfigSettingWithUI, ConfigSettingWithUIController, MenuLookup}

import scalafx.geometry.Rectangle2D
import scalafx.scene.control.{Accordion, ButtonType, Dialog, TitledPane}
import scalafx.scene.image.ImageView

import com.github.sesquipedalian_dev.jukebox.engine.ui.I18nManager._

case class PreferencesModal()(implicit gameLoop: GameLoop) {
  val UI_ICONS_FILE = "/fxml/ui-icons-222222-256x240.png"
  val UI_SETTINGS_ICON_BOUNDS = new Rectangle2D(196.0, 116.0, 10.0, 10.0)

  def saveSettings(settings: List[ConfigSettingWithUIController]): Boolean = {
    val updated = settings.foldLeft(false)((soFar, next) => {
      next.onSave() /* side effects */ || soFar
    })

    if(updated) {
      ConfigSettings.saveUserConfig()
    }
    updated
  }

  // find the menu item that should load up our modal
  val preferencesMenu = MenuLookup.oneLevelLookup(Main.stage.scene(), "fileMenu", "prefMenu")

  preferencesMenu.foreach(prefMenu => {
    prefMenu.setOnAction(new EventHandler[ActionEvent] {
      override def handle(event: ActionEvent) = {
        /*************************************
          * when pref menu clicked, create dialog to edit user prefs
          */
        val diag = new Dialog[Boolean]() {
          title = L("preferences.modal.title")
          headerText = L("preferences.modal.header")
          resizable = true
        }

        // attach an image
        val img = new ImageView(this.getClass().getResource(UI_ICONS_FILE).toString)
        img.viewport = UI_SETTINGS_ICON_BOUNDS
        diag.setGraphic(img)

        // create buttons
        diag.dialogPane.value.getButtonTypes.addAll(
          ButtonType.OK, ButtonType.Cancel
        )

        val okButton = diag.dialogPane.value.lookupButton(ButtonType.OK)

        // create editable settings based on current settings' values
        val contentPane = new Accordion() {
        }

        // let all the user-editable config settings generate their UI controller
        val configUIControllers = ConfigSettings.configSettings collect {
          case x: ConfigSettingWithUI => {
            val controller = x.createUI(okButton)
            contentPane.panes.add((new TitledPane() {
              delegate.setText(controller.label)
              content = controller.node
            }).delegate)
            controller
          }
        }

        // attach editing pane content to dialog
        diag.dialogPane.delegate.getValue.setContent(contentPane)

        diag.dialogPane.value.setPrefSize(800.0, 600.0)

        // add result processor
        diag.resultConverter = (dialogButton) => {
          gameLoop.unpause()
          dialogButton match {
            case ButtonType.OK => {
              saveSettings(configUIControllers)
            }
            case ButtonType.Cancel => {
              false
            }
            case bt => {
              // TODO log error
              println("Unkonwn button type" + bt)
              false
            }
          }
        }

        // show the constructed dialog as a modal
        gameLoop.pause()
        diag.showAndWait()
      }
    })
  })
}
