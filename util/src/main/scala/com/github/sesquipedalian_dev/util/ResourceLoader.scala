/**
  * Copyright 2/5/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.util

import javafx.scene.image.Image

import com.typesafe.scalalogging.LazyLogging

// utility for loading resource from multiple class loaders
object ResourceLoader extends LazyLogging {
  var loaders: List[ClassLoader] = List(getClass.getClassLoader)

  def loadImage(url: String): Option[Image] = {
    loaders.foldLeft(Option.empty[Image])((soFar, next) => {
      soFar orElse {
        try {
          val stream = next.getResourceAsStream(url)
          Some(new Image(stream))
        } catch {
          case x: Throwable => {
            logger.trace("Couldn't load image from this loader {} {}", next, url)
            None
          }
        }
      }
    })
  }

  def installResourceLoader(loader: ClassLoader): Unit = {
    loaders :+= loader
  }
}
