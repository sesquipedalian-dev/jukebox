/**
  * Created by Scott on 2/5/2017.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.{Renderer, Updater}
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.GameLoopModule._
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.SceneObject
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.ObjectsModule._
import com.typesafe.scalalogging.LazyLogging

import scalafx.geometry.Point2D


case class AsteroidCollisionWatcher(
  size: String,
  var ticsExemptFromCollision: Int = 30 // ms
)
  extends Updater
  with LazyLogging
{
  override def update(eid: EntityIdType)(implicit ecs: ECS[UUIDIdType]): Unit = {
    // early out if we haven't been on-screen long enough
    ticsExemptFromCollision -= 1
    if(ticsExemptFromCollision > 0) return

    ecs.system[SceneObject].get(eid).foreach(sceneObject => {
      for {
        (bulletEid, _) <- ecs.system[Renderer]
        bulletObj <- ecs.system[SceneObject].get(bulletEid)
        bulletPos <- bulletObj.polygon.headOption if
          sceneObject.containsPoint(new Point2D(bulletPos.x, bulletPos.y))
      } {
        logger.info("Handling asteroid collision ! {" + size + "}{}", sceneObject.polygon.head)
        // if any bullets' positions are contained within our polygon,
        // time to asplode

        // find possible smaller asteroid size(s) to spawn
        val maybeNewAsteroidParams = size match {
          case "large" => ASTEROID_PARAMS_MAP().get("medium")
          case "medium" => ASTEROID_PARAMS_MAP().get("small")
          case _ => None
        }

        // if desired, spawn two new asteroids at our position
        maybeNewAsteroidParams.foreach(params => {
          List.fill(2)(AsteroidsModule.instance.spawnAsteroid(
            _params = Some(params),
            _initialPosition = Some(sceneObject.polygon.head))
          )
        })

        // remove this asteroid obj
        ecs -= eid
      }
    })
  }
}
