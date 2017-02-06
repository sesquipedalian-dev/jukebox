/**
  * Created by Scott on 2/6/2017.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import javafx.scene.canvas.GraphicsContext
import javafx.scene.text.Font

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Renderer
import com.github.sesquipedalian_dev.util.ResourceLoader
import com.github.sesquipedalian_dev.jukebox.module.asteroids.AsteroidsModule._
import javafx.scene.image.Image

import com.github.sesquipedalian_dev.jukebox.engine.components.objects.SceneController
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D

import scalafx.scene.paint.Color

case class PlayerRenderer() extends Renderer {
  var extraLifeImg: Option[Image] = None

  override def renderOrder(ecs: ECS[UUIDIdType], eid: EntityIdType): Int = 0
  override def render(eid: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit = {
    ecs.system[Player].getOrElse(eid, Nil).foreach(player => {
      // render score
      gc.setLineWidth(2)
      gc.setStroke(Color.White)
      gc.setFont(new Font(gc.getFont.getName, 15))
      gc.strokeText(player.score.toString, 50, 50)

      // render lives remaining
      if(extraLifeImg.isEmpty) {
        extraLifeImg = ResourceLoader.loadImage("img/extra_life.jpg")
      }
      var elPosition = SerializablePoint2D(0, 75)
      var livesRemaining = player.livesRemaining
      while(livesRemaining > 0) {
        extraLifeImg.foreach(image => {
          gc.drawImage(image, elPosition.x, elPosition.y)
        })
        elPosition = SerializablePoint2D(elPosition.x + 60, elPosition.y)
        livesRemaining -= 1
      }

      // render player avatar itself
      // TODO
    })
  }
}
