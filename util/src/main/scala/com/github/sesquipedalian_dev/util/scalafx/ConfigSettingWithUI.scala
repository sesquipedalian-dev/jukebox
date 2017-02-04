/**
  * Copyright 11/27/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.util.scalafx

import com.github.sesquipedalian_dev.util.config._

import scalafx.scene.Node
import scalafx.scene.control.{CheckBox, TextField, TextInputControl}

// A user-facing config setting that can be edited in the UI
trait ConfigSettingWithUI {
  def createUI(okButton: javafx.scene.Node): ConfigSettingWithUIController
}

trait ConfigSettingWithUIController {
  def label: String
  def node: Node
  def onSave(): Boolean // return true if we ended up saving changes
}

trait BoolConfigSettingWithUI extends ConfigSettingWithUI { this: LoadableBooleanConfigSetting =>
  def createUI(okButton: javafx.scene.Node): ConfigSettingWithUIController = {
    val origValue = getValue
    val cb = new CheckBox() {
      selected = origValue
    }

    new ConfigSettingWithUIController {
      val label = configFileName
      val node = cb

      override def onSave(): Boolean = {
        if(cb.selected.value != origValue) {
          value = Some(cb.selected().booleanValue())
          true
        } else {
          false
        }
      }
    }
  }
}

trait ConfigSettingWithStringUI[T] extends ConfigSettingWithUI { this: LoadableConfigSetting[T] =>
  // template method for string validation - if user edits value and it fails this validation,
  // we'll disable setting that config (the ok button)
  // true if valid, false if not valid
  // this can also throw, in which case the field will be marked invalid
  def validateString(tryValue: String): Boolean

  // template method for setting our value after the user hits OK in the UI
  def finalizeValueFromString(newValue: String): Unit

  def createUI(okButton: javafx.scene.Node): ConfigSettingWithUIController = {
    val origValue = getValue
    val tf = new TextField() {
      text = origValue.toString
    }

    tf.text.onChange((observable, oldValue, newValue) => {
      val shouldDisable = try {
        !validateString(newValue)
      } catch {
        case e: Exception => true
      }
      okButton.setDisable(shouldDisable)
    })

    new ConfigSettingWithUIController {
      val label = configFileName
      val node = tf

      override def onSave(): Boolean = {
        if (tf.text.value != origValue.toString) {
          try {
            finalizeValueFromString(tf.text())
            true
          } catch {
            case e: NumberFormatException => false
          }
        } else {
          false
        }
      }
    }
  }
}

trait DoubleConfigSettingWithUI extends ConfigSettingWithStringUI[Double]  { this: LoadableDoubleConfigSetting =>
  override def validateString(tryValue: String): Boolean = {
    tryValue.toDouble
    true
  }

  override def finalizeValueFromString(newValue: String): Unit = {
    value = Some(newValue.toDouble)
  }
}

trait StringConfigSettingWithUI extends ConfigSettingWithStringUI[String] { this: LoadableStringConfigSetting =>
  def validateString(tryValue: String): Boolean = {
    true
  }

  def finalizeValueFromString(newValue: String): Unit = {
    value = Some(newValue)
  }
}
