/**
  * Copyright 11/28/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.util.scalafx


import java.io.{PrintWriter, StringWriter}

import scalafx.scene.control.{Alert, ButtonType, Label, TextArea}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.layout.{GridPane, Priority}

/**
  * adapted from http://code.makery.ch/blog/javafx-dialogs-official/
  */
object SimpleModalDialogs {

  // adapted from # Confirmation Dialog
  def showConfirm(message: String) : Boolean = {
    val alert = new Alert(AlertType.Confirmation) {
      title = "Confirmation Dialog"
      headerText = "Confirmation Dialog"
      contentText = message
    }

    val result = alert.showAndWait()
    result.contains(ButtonType.OK)
  }

  // adapted from # Exception Dialog
  def showException(message: String, ex: Exception): Unit = {
    val alert = new Alert(AlertType.Error) {
      title = "Exception Dialog"
      headerText = "ERROR"
      contentText = message
    }

    // Create expandable Exception.
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    ex.printStackTrace(pw)
    val exceptionText = sw.toString()

    val label = new Label("The exception stacktrace was:");

    val textArea = new TextArea(exceptionText) {
      editable = false
      wrapText = true
      maxWidth = Double.MaxValue
      maxHeight = Double.MaxValue
    }

    GridPane.setVgrow(textArea, Priority.Always)
    GridPane.setHgrow(textArea, Priority.Always)

    val expContent = new GridPane() {
      maxWidth = Double.MaxValue
      add(label, 0, 0)
      add(textArea, 0, 1)
    }

    // Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent)

    alert.showAndWait()
  }
}
