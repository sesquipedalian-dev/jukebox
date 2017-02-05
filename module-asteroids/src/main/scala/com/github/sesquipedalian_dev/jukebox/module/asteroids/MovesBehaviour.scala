/**
  * Copyright 2/5/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.{CANVAS_HEIGHT, CANVAS_WIDTH, UUIDIdType}
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Updater
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.ObjectsModule._
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.SceneObject
import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D
import com.typesafe.scalalogging.LazyLogging

case class MovesBehaviour(
  destroyAfterDistance: Option[Double],
  velocityVector: SerializablePoint2D,
  var distanceTraveled: Double = 0.0
) extends Updater
  with LazyLogging
{
  override def update(eid: EntityIdType)(implicit ecs: ECS[UUIDIdType]): Unit = {
    ecs.system[SceneObject].get(eid).foreach(sceneObject => {
      val velocityAngle = Math.atan2(velocityVector.y, velocityVector.x)
      val distanceThisTick = velocityVector.x / Math.cos(velocityAngle)
      distanceTraveled += Math.abs(distanceThisTick)
      logger.trace("checking distance traveled by movesbehaviour {" +
        distanceTraveled + "}{" + distanceThisTick + "}")

      if(destroyAfterDistance.map(_ <= distanceTraveled).getOrElse(false)) {
        ecs -= eid
      } else {
        val applyVelocity = sceneObject.polygon.map(point => point + velocityVector)
        val checkBounds = List(checkXMax _, checkYMax _, checkXMin _, checkYMin _)
          .foldLeft(applyVelocity)((soFar, next) => {
            next(soFar)
          })
        sceneObject.polygon = checkBounds
      }
    })
  }

  def checkXMax(polygon: List[SerializablePoint2D]): List[SerializablePoint2D] = {
    val outOfBoundsPoint = polygon.find(p => p.x > CANVAS_WIDTH())
    if(outOfBoundsPoint.nonEmpty) {
      val width = polygon.drop(1).head.x - polygon.head.x
      List(
        SerializablePoint2D(0, polygon.head.y),
        SerializablePoint2D(width, polygon.head.y),
        SerializablePoint2D(width, polygon.last.y),
        SerializablePoint2D(0, polygon.last.y)
      )
    } else {
      polygon
    }
  }

  def checkYMax(polygon: List[SerializablePoint2D]): List[SerializablePoint2D] = {
    val outOfBoundsPoint = polygon.find(p => p.y > CANVAS_HEIGHT())
    if(outOfBoundsPoint.nonEmpty) {
      val height = polygon.last.y - polygon.head.y
      List(
        SerializablePoint2D(polygon.head.x, 0),
        SerializablePoint2D(polygon.drop(1).head.x, 0),
        SerializablePoint2D(polygon.drop(1).head.x, height),
        SerializablePoint2D(polygon.head.x, height)
      )
    } else {
      polygon
    }
  }

  def checkXMin(polygon: List[SerializablePoint2D]): List[SerializablePoint2D] = {
    val outOfBoundsPoint = polygon.find(p => p.x < 0)
    if(outOfBoundsPoint.nonEmpty) {
      val width = polygon.drop(1).head.x - polygon.head.x
      logger.info("checking bounds is width neg? {}", width)
      List(
        SerializablePoint2D(CANVAS_WIDTH() - width, polygon.head.y),
        SerializablePoint2D(CANVAS_WIDTH(), polygon.head.y),
        SerializablePoint2D(CANVAS_WIDTH(), polygon.last.y),
        SerializablePoint2D(CANVAS_WIDTH() - width, polygon.last.y)
      )
    } else {
      polygon
    }
  }

  def checkYMin(polygon: List[SerializablePoint2D]): List[SerializablePoint2D] = {
    val outOfBoundsPoint = polygon.find(p => p.y < 0)
    if(outOfBoundsPoint.nonEmpty) {
      val height = polygon.last.y - polygon.head.y
      logger.info("checking bounds is height neg? {}", height)
      List(
        SerializablePoint2D(polygon.head.x, CANVAS_HEIGHT() - height),
        SerializablePoint2D(polygon.drop(1).head.x, CANVAS_HEIGHT() - height),
        SerializablePoint2D(polygon.drop(1).head.x, CANVAS_HEIGHT()),
        SerializablePoint2D(polygon.head.x, CANVAS_HEIGHT())
      )
    } else {
      polygon
    }
  }
}
