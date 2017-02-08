/**
  * Created by Scott on 2/5/2017.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.module.asteroids.AsteroidsModule._
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


    ecs.system[SceneObject].getOrElse(eid, Nil).foreach(sceneObject => {
      // check for player collision
      var collidedWithPlayer: Boolean = false

      val isPlaying = ecs.system[Updater].toList.flatMap(kvp => kvp._2.map(v => (kvp._1 -> v))).collect({case (k, v: AsteroidsGlobalController) => (k, v)}).exists(kvp => {
        val (id, globalController) = kvp
        globalController.state == GlobalControllerState.PLAYING
      })
      if(isPlaying) {
        ecs.system[Player].toList.flatMap(kvp => kvp._2.map(v => (kvp._1 -> v))).foreach(playerStruct => {
          val (pid, player) = playerStruct
          val playerInvincible = player.playingDeathAnim > 0
          val weCollided = sceneObject.containsPoint(new Point2D(player.position.x, player.position.y))
          if ((!playerInvincible) && weCollided) {
            logger.info("Handling player collision!")
            ecs -= eid
            player.hitByObject(pid, ecs)
            collidedWithPlayer = true
          }
        })
      }

      // check for bullet positions
      for {
        (bulletEid, rList) <- ecs.system[Renderer] if rList.exists(_.isInstanceOf[BulletRenderer]) if !collidedWithPlayer
        bulletObj <- ecs.system[SceneObject].getOrElse(bulletEid, Nil)
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

        // remove the collided objects
        ecs -= eid
        ecs -= bulletEid

        // count score
        ecs.system[Player].foreach(kvp => {
          val (key, lst) = kvp
          lst.foreach(playerObj => {
            playerObj.addScore(ASTEROID_PARAMS_MAP().get(size).map(_.scoreForDestroy).getOrElse(0))
          })
        })
      }
    })
  }
}
