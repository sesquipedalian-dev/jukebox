/**
  * Created by Scott on 2/5/2017.
  */
package com.github.sesquipedalian_dev.jukebox.module.asteroids

import javafx.scene.canvas.GraphicsContext

import com.github.gigurra.scalego.core.ECS
import com.github.sesquipedalian_dev.jukebox.engine.UUIDIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.EntityIdType
import com.github.sesquipedalian_dev.jukebox.engine.components.gameloop.Renderer
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.SceneObject
import com.github.sesquipedalian_dev.jukebox.engine.components.objects.ObjectsModule._

import scalafx.scene.paint.Color

case class BulletRenderer() extends Renderer {
  override def renderOrder(ecs: ECS[UUIDIdType], eid: EntityIdType): Int = 0

  override def render(eid: EntityIdType, gc: GraphicsContext)(implicit ecs: ECS[UUIDIdType]): Unit = {
    ecs.system[SceneObject].getOrElse(eid, Nil).foreach(sceneObject => {
      val position = sceneObject.polygon.head
      gc.setStroke(Color.Red)
      gc.setFill(Color.Red)
      gc.setLineWidth(10)
      gc.fillOval(position.x, position.y, 10, 10)
    })
  }
}
