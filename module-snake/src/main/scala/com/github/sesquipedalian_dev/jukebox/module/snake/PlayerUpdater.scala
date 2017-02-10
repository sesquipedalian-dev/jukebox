/**
  * Created by Scott on 2/9/2017.
  */
package com.github.sesquipedalian_dev.jukebox.module.snake

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.{Renderer, Updater}
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.GameLoopModule._
import com.github.sesquipedalian_dev.jukebox.module.snake.SnakeModule._
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D
import com.typesafe.scalalogging.LazyLogging

case class PlayerUpdater(
) extends Updater
  with LazyLogging
{
  override def update(eid: EntityIdType)(implicit ecs: ECS[UUIDIdType]): Unit = {
    ecs.system[Player].getOrElse(eid, Nil).foreach(player => {
      val currentPos = player.segments.head
      val nextSpace = movePointToDirection(player.directionOfTravel, currentPos)

      if(nextSpace.x < 0 || // out of bounds left
        nextSpace.x > CANVAS_PIXELS_WIDTH() || // out of bounds right
        nextSpace.y < 0 || // out of bounds up
        nextSpace.y > CANVAS_PIXELS_HEIGHT() || // out of bounds down
        player.segments.contains(nextSpace) // collision with other snake parts
      ){
        ecs.system[Updater].toList.flatMap(kvp => kvp._2.map(v => kvp._1 -> v)).collect({case (k, v: SnakeInputController) => { v } }).foreach(inputController => {
          inputController.state = GlobalControllerState.DIED
        })
      } else {
        // if space is a the pellet add it to our pending pellets
        ecs.system[Renderer].toList.flatMap(kvp => kvp._2.map(v => kvp._1 -> v)).foreach({
          case (k, x: PelletRenderer) if x.position == nextSpace => {
            ecs -= k
            player.pendingTail = player.pendingTail :+ x.position
            SnakeModule.instance.spawnPellet(ecs)
          }
          case _ =>
        })

        // calculate movement of player avatar on each tick
        var currentDir = player.directionOfTravel
        var turns = player.turns

        val newSegmentSeq = for {
          segment <- player.segments
        } yield {
          // move the segment in the current movement direction
          val newSegment = movePointToDirection(currentDir, segment)

          // if the segment is a turning point, change the direction of travel and remove the turn
          if (turns.headOption.map(t => t.point == segment).getOrElse(false)) {
            currentDir = turns.head.direction
            turns = turns.tail
          }

          // if segment is the last point and we have a pending tail, tack it on to our segments
          if (segment == player.segments.last && player.pendingTail.headOption.map(pt => pt == segment).getOrElse(false)) {
            val result = newSegment :: player.pendingTail.head :: Nil
            player.pendingTail = player.pendingTail.tail
            result
          } else {
            newSegment :: Nil
          }
        }
        player.segments = newSegmentSeq.flatten
      }
    })
  }

  def movePointToDirection(direction: Direction.DirectionType, point: SerializablePoint2D): SerializablePoint2D = {
    direction match {
      case Direction.Up => SerializablePoint2D(point.x, point.y - 1)
      case Direction.Down => SerializablePoint2D(point.x, point.y + 1)
      case Direction.Left => SerializablePoint2D(point.x - 1, point.y)
      case Direction.Right => SerializablePoint2D(point.x + 1, point.y)
    }
  }
}
