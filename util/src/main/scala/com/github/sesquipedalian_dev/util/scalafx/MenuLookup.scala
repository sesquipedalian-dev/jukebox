/**
  * Copyright 11/27/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.util.scalafx

import java.util.function.Predicate
import javafx.scene.Scene
import javafx.scene.control.{Menu, MenuItem}

// utility to help with finding menu items in javafx scene
object MenuLookup {
  def menuBarLookup(scene: Scene): Option[javafx.scene.control.MenuBar] = {
    scene.lookup("#menuBar") match {
      case n if n == null => None
      case n: javafx.scene.control.MenuBar => Some(n)
    }
  }

  def topLevelLookup(scene: Scene, menuHeading: String): Option[Menu] = {
    menuBarLookup(scene).flatMap(n => {
      val menuWithHeading = n.getMenus().filtered(new Predicate[javafx.scene.control.Menu] {
        override def test(t: Menu): Boolean = t.getId() == menuHeading
      })
      if(menuWithHeading.size > 0) {
        Some(menuWithHeading.get(0))
      } else {
        None
      }
    })
  }

  def oneLevelLookup(scene: Scene, menuHeading: String, menuItem: String): Option[MenuItem] = {
    topLevelLookup(scene, menuHeading).flatMap(menuWithHeading => {
      val menuItemWithName = menuWithHeading.getItems().filtered(new Predicate[javafx.scene.control.MenuItem] {
        override def test(t: MenuItem): Boolean = t.getId() == menuItem
      })
      if (menuItemWithName.size > 0) {
        Some(menuItemWithName.get(0))
      } else {
        None
      }
    })
  }
}
