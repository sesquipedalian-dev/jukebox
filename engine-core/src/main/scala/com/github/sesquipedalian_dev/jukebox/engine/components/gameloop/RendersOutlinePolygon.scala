/**
  * Copyright 12/17/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components.gameloop

import javafx.scene.canvas.GraphicsContext

import com.github.sesquipedalian_dev.jukebox.engine.components.objects.{SceneController, SceneObject}

import scalafx.scene.paint.Color

//
trait RendersOutlinePolygon {
  def renderHighlight(sceneObject: SceneObject, gc: GraphicsContext): Unit = {
    if (sceneObject.isHighlighting) {
      gc.setStroke(Color.GreenYellow)
      gc.setLineWidth(10)
      sceneObject.segments.foreach(segment => {
        gc.strokeLine(
          segment.head.x - SceneController.viewport.head.x,
          segment.head.y - SceneController.viewport.head.y,
          segment.last.x - SceneController.viewport.head.x,
          segment.last.y - SceneController.viewport.head.y
        )
      })
    }
  }
}
