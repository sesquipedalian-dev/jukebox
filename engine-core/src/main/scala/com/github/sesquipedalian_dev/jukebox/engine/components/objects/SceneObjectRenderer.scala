/**
  * Copyright 12/2/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components.objects

import javafx.scene.canvas.GraphicsContext

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.{Renderer, RendersOutlinePolygon}
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.ObjectsModule._
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.util.ResourceLoader
import com.typesafe.scalalogging.LazyLogging

import scalafx.scene.image.Image
import scalafx.scene.paint.Color

case class SceneObjectRenderer(
) extends Renderer
  with LazyLogging
  with RendersOutlinePolygon
{
  def renderOrder(ecs: ECS[UUIDIdType], eid: UUIDIdType#EntityId): Int = {
    // use the scene object's z-sort
    ecs.system[SceneObject].get(eid).map(scene => {
      scene.zSort
    }).getOrElse(0)
  }

  var currentFrame: Int = 0 // for animations

  var loadedStaticImage: Option[Image] = None
  var loadedAnimationImages: List[Image] = Nil

  override def render(eid: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit = {
    logger.trace(s"SOR start render")
    ecs.system[SceneObject].get(eid).foreach(sceneObject => {
      logger.trace(s"SOR img1? ${sceneObject.currentAnimation}")

      // pick image to render depending on whether it's an animation or a static sprite currently
      val img = if(sceneObject.currentAnimation.nonEmpty) {
        // load up / split spritesheet if needed
        if(loadedAnimationImages.isEmpty) {
          loadedAnimationImages = sceneObject.currentAnimation.get.getParts()
        }

        // calculate which frame of our list to show now
        logger.trace(s"SOR img2? ${loadedAnimationImages}")
        val calculatedFrame = (currentFrame / sceneObject.currentAnimation.get.framesPerImg) % loadedAnimationImages.size
        val res = Some(loadedAnimationImages(calculatedFrame))

        // increment frame count, loop around if we use all frames
        currentFrame += 1
        if(currentFrame > (loadedAnimationImages.size * sceneObject.currentAnimation.get.framesPerImg)) {
          currentFrame = 0
        }

        res
      } else if (sceneObject.texturePath.nonEmpty) {
        if(loadedStaticImage.isEmpty) {
          val image = sceneObject.texturePath.get
          loadedStaticImage = ResourceLoader.loadImage(image).map(img => new scalafx.scene.image.Image(img))
        }
        loadedStaticImage
      } else {
        None
      }

      // if 'highlight objects' key is down, draw a highlight around our polygon box
      logger.trace("are we highlighting!?!? {} {}", sceneObject.isHighlighting)
      logger.trace("higlighting!? {}", sceneObject.segments)
      renderHighlight(sceneObject, gc)

      img.foreach(image => {
        // display our image at the correct coords within the scene (adjust by viewport)
        gc.drawImage(
          image,
          sceneObject.polygon.head.x - SceneController.viewport.head.x,
          sceneObject.polygon.head.y - SceneController.viewport.head.y
        )
      })

      if(sceneObject.isMousedOver) {
        sceneObject.mouseOverText.foreach(mot => {
          val baseX = sceneObject.polygon.head.x - SceneController.viewport.head.x
          val baseY = sceneObject.polygon.head.y - SceneController.viewport.head.y
          gc.setStroke(Color.Black)
          gc.setFill(Color.Black)
          gc.setLineWidth(2)
          gc.fillRect(baseX, baseY + 10, mot.size * 9, 20)

          gc.setStroke(Color.White)
          gc.strokeText(mot, baseX + 5, baseY + 25)
        })
      }
    })
  }
}
