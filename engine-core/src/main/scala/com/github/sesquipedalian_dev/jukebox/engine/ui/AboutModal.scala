/**
  * Copyright 11/27/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.ui

import java.io.{BufferedReader, IOException, InputStreamReader}
import javafx.event.{ActionEvent, EventHandler}

import com.github.sesquipedalian_dev.jukebox.engine._
import com.github.sesquipedalian_dev.util.scalafx.MenuLookup
import com.typesafe.scalalogging.LazyLogging

import scalafx.scene.control.{Accordion, ButtonType, Dialog, TitledPane}
import scalafx.scene.text.Text

import com.github.sesquipedalian_dev.jukebox.engine.ui.I18nManager._

case class AboutModal()(implicit gameLoop: GameLoop) extends LazyLogging {
  val textFilesToLoad = List(
    "developer.statement" -> "/aboutInfo/summary.txt",
    "logback.license" -> "/aboutInfo/logback/Eclipse Public License - Version 1.0.html",
    "scalafx.license" -> "/aboutInfo/scalafx/LICENSE.txt",
    "scalego.license" -> "/aboutInfo/scalego/LICENSE",
    "typesafe.config.license" -> "/aboutInfo/typesafehub.config/LICENSE-2.0.txt",
    "typesafe.logging.license" -> "/aboutInfo/typesafehub.scala-logging/LICENSE.txt"
  )

  // find the menu item that should load up our modal
  val aboutMenu = MenuLookup.oneLevelLookup(Main.stage.scene(), "helpMenu", "aboutMenu")

  aboutMenu.foreach(menu => {
    menu.setOnAction(new EventHandler[ActionEvent] {
      override def handle(event: ActionEvent) = {
        /*************************************
          * when about menu clicked, create a dialog to display some text blurbs
          */
        val diag = new Dialog[Boolean]() {
          title = L("about.modal.title")
          headerText = L("about.modal.header")
          resizable = true
        }

        // create buttons
        diag.dialogPane.value.getButtonTypes.addAll(
          ButtonType.OK, ButtonType.Cancel
        )

        // create editable settings based on current settings' values
        val contentPane = new Accordion() {
        }

        textFilesToLoad.foreach(pair => {
          val (key, filename) = pair
          logger.debug(s"loading resource file ${filename}")
          val c = getClass()
          val r = c.getResource(filename)
          val resourceStream = r.openStream()
          val br = new BufferedReader(new InputStreamReader(resourceStream))

          val sb = new StringBuffer()
          try {
            var done = false
            do {
              val currentLine = br.readLine()
              if(currentLine != null) {
                sb.append(currentLine)
                sb.append("\n")
              } else {
                done = true
              }
            } while(!done)
          } catch {
            case e: IOException => {
              e.printStackTrace()
            }
          }

          val node = new TitledPane() {
            delegate.setText(key)
            val tx = new Text() {
              text = sb.toString
              wrapText = true
            }
            tx.maxHeight(WINDOW_HEIGHT.getValue)
            tx.maxWidth(WINDOW_WIDTH.getValue)
            content = tx
          }
          contentPane.panes.add(node)
        })

        // attach editing pane content to dialog
        diag.dialogPane.delegate.getValue.setContent(contentPane)

        diag.dialogPane.value.setMaxSize(800.0, 600.0)

        // add result processor
        diag.resultConverter = (dialogButton) => {
          gameLoop.unpause()
          true
        }

        // show the constructed dialog as a modal
        gameLoop.pause()
        diag.showAndWait()
      }
    })
  })
}
