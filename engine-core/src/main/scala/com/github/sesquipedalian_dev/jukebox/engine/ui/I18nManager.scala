/**
  * Copyright 12/10/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.ui

import java.util.{Locale, MissingResourceException, ResourceBundle}
import javafx.event.{ActionEvent, Event, EventHandler}
import javafx.scene.control.{Menu, MenuItem}

import com.github.sesquipedalian_dev.jukebox.engine.Main
import com.github.sesquipedalian_dev.util.config.{ConfigSetting, ConfigSettings, LoadableConfigSetting}
import com.github.sesquipedalian_dev.util.scalafx.{MenuLookup, SimpleModalDialogs}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scalafx.scene.control.{RadioMenuItem, ToggleGroup}
import scalafx.scene.image.{Image, ImageView}

case object RESOURCE_BUNDLE_NAME extends ConfigSetting[String] {
  override def defaultValue: String = "i18n.jukebox"
  override def populateFromConfig(newConfig: Config): Unit = {} // Nop
}

case object DEFAULT_LOCALE extends LoadableConfigSetting[Locale] {
  override def defaultValue: Locale = new Locale("en", "US")
  override def userSetting: Boolean = true
  override def configFileName: String = "jukebox.locale"

  override protected def configGetMethod(config: Config): Locale = {
    val parts = config.getString(configFileName).split("_").toList
    new Locale(parts.head, parts.last)
  }
}

case object I18nManager extends LazyLogging {
  def L(key: String): String = {
    currentLocBundle.getString(key)
  }

  def currentLocBundle: ResourceBundle = {
    getLocBundle(Locale.getDefault)
  }

  // TODO memoize for performance?
  private def getLocBundle(locale: Locale): ResourceBundle = {
    try {
      ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME.getValue, locale)
    } catch {
      case x: MissingResourceException => {
        logger.warn("Requested load of locale for which we don't have resources.  Default to english! {} {}", locale, x.getMessage)
        Locale.setDefault(DEFAULT_LOCALE())
        SimpleModalDialogs.showException(L("exception.language_unsupported"), x)
        currentLocBundle
      }
    }
  }

  def availableLocales(): List[Locale] = {
    Locale.getAvailableLocales.filter(javaLocale => {
      val resourcePath = "/i18n/jukebox_" + javaLocale.getLanguage + ".properties"
      logger.trace("trying to locate i18n file: {}", resourcePath)
      getClass().getResource(resourcePath) != null
    }).groupBy(_.getLanguage).map(_._2.sortWith((lhs, rhs) => {
      lhs == currentLocale // try to get the user's initial locale set as the default selection of the menu item
    }).head).toList
  }

  def setLocale(locale: Locale, isInitialLoad: Boolean = false): Unit = {
    logger.info("requested switch locale {}", locale)
    if(!isInitialLoad) {
      // hold onto new and old loc bundle so that we can lookup keys in both
      val oldBundle = currentLocBundle
      val newBundle = getLocBundle(locale)

      // go through the menu bar, and update the text for any items that have a value in the old bundle
      // this may hit false positives but is quick and dirty for our application
      MenuLookup.menuBarLookup(Main.theScene).foreach(menuBar => {
        var menuItems: List[MenuItem] = Nil
        menuBar.getMenus.toArray.toList.map(_.asInstanceOf[Menu]).foreach(menu => {
          menuItems :+= menu
          menuItems ++= menu.getItems.toArray.toList.map(_.asInstanceOf[MenuItem]) // TODO surely there are easier implicit java list conversions
        })
        menuItems.foreach(menu => {
          val keys = oldBundle.keySet.toArray.toList.map(_.asInstanceOf[String]) // TODO surely there are easier implicit java list conversions
          val foundKey = keys.find(k => {
            oldBundle.getString(k) == menu.getText
          })
          foundKey.foreach(k => {
            menu.setText(newBundle.getString(k))
          })
        })
      })
    }

    // set default locale so any modals or such that look for a string will use updated locale
    Locale.setDefault(locale)
    DEFAULT_LOCALE.value = Some(locale)
    ConfigSettings.saveUserConfig()
  }

  def currentLocale: Locale = Locale.getDefault

  // initialization - create menu item to select localization
  def apply(): Unit = {
    setLocale(DEFAULT_LOCALE.getValue)

    MenuLookup.topLevelLookup(Main.theScene, "i18nMenu").foreach(menu => {
      val theToggleGroup = new ToggleGroup

      val newMenuItems = availableLocales().map(l => {
        // create a 'radio' button for each supported locale, with an icon representing that language
        val menuItem = new RadioMenuItem(l.getLanguage.toUpperCase) {
          graphic = getImageFileForLocale(l)
          selected = l == currentLocale
          toggleGroup = theToggleGroup
        }

        // when modified, if this button is selected, set the locale and reload
        menuItem.onAction = new EventHandler[ActionEvent]() {
          override def handle(event: ActionEvent): Unit = {
            if(menuItem.selected() && (l != currentLocale)) {
              setLocale(l)
            }
          }
        }

        l -> menuItem
      })

      // set the initial icon for the top-level menu item based on the defaulted locale
      newMenuItems.foreach(pair => {
        val (locale, item) = pair
        menu.getItems.add(item)
        if(item.selected()) {
          menu.setGraphic(getImageFileForLocale(locale))
        }
      })

      // if menu is hidden (assumably a radio option was selected), update the top-level menu item to the selected locale again
      val eh = new EventHandler[Event]() {
        override def handle(event: Event): Unit = {
          newMenuItems.foreach(pair => {
            val (locale, item) = pair
            if(item.selected()) {
              menu.setGraphic(getImageFileForLocale(locale))
            }
          })
        }
      }
      menu.setOnHidden(eh)
    })
  }

  private def getImageFileForLocale(locale: Locale): ImageView = {
    new ImageView(new Image("/i18n/imgs/" + locale.getLanguage + ".jpg", 16.0, 16.0, preserveRatio = false, smooth = false))
  }
}
