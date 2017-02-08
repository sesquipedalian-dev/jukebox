/**
  * Created by Scott on 2/6/2017.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import javafx.geometry.Point2D
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
import com.typesafe.scalalogging.LazyLogging

import scalafx.scene.paint.Color

case class PlayerRenderer()
  extends Renderer
  with LazyLogging
{
  var extraLifeImg: Option[Image] = None

  override def renderOrder(ecs: ECS[UUIDIdType], eid: EntityIdType): Int = 1000
  override def render(eid: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit = {
    ecs.system[Player].getOrElse(eid, Nil).foreach(player => {
      // render score
      gc.setLineWidth(2)
      gc.setStroke(Color.White)
      gc.setFont(new Font(gc.getFont.getName, 15))
      gc.strokeText(player.score.toString, 50, 50)

      // render lives remaining
      if(extraLifeImg.isEmpty) {
        extraLifeImg = ResourceLoader.loadImage("img/extra_life.gif")
      }
      var elPosition = SerializablePoint2D(10, 75)
      var livesRemaining = player.livesRemaining
      while(livesRemaining > 0) {
        extraLifeImg.foreach(image => {
          gc.drawImage(image, elPosition.x, elPosition.y)
        })
        elPosition = SerializablePoint2D(elPosition.x + 35, elPosition.y)
        livesRemaining -= 1
      }

      // render player avatar itself
      logger.trace("player lives remaining {" + player.livesRemaining + "}")
      if(player.livesRemaining <= 0) {
        // don't render when dead
      } else {
        if (player.playingDeathAnim > 0) {
          player.playingDeathAnim -= 1
          if (player.playingDeathAnim % 10 > 5) {
            renderAvatar(player, gc)
          } else {
            // don't play on some of the frames to generate a blinky effect
          }
        } else {
          renderAvatar(player, gc)
        }
      }
    })
  }

  def renderAvatar(player: Player, gc: GraphicsContext): Unit = {
    val position = player.position
    val points = playerAvatarPoints
    val rotatePoints = points.map(p => p.rotate(player.rotationRadians))
    val positionedPoints = rotatePoints.map(p => SerializablePoint2D(position.x + p.x, position.y + p.y))
    val lineSegments = (positionedPoints :+ positionedPoints.head /* connect back up to start */).sliding(2).toList
    logger.trace("player segments {}", lineSegments)
    gc.setLineWidth(5)
    gc.setStroke(Color.Yellow)
    lineSegments.foreach(segment => {
      logger.trace("stroking segment {} {}", segment.head, segment.last)
      gc.strokeLine(segment.head.x, segment.head.y, segment.last.x, segment.last.y)
    })
  }

  // points that make up the player avatar, relative to the player's position
  private val playerAvatarPoints = List[SerializablePoint2D](
    SerializablePoint2D(25, 0), // tip of spaceship
    SerializablePoint2D(-25, 15), // tip of left wing
    SerializablePoint2D(-15, 0), // cleft in rear of ship
    SerializablePoint2D(-25, -15) // tip of right wing
  )
}
