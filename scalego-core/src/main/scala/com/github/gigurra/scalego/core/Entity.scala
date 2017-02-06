package com.github.gigurra.scalego.core

import Entity.HasNoSuchComponent

import scala.collection.mutable
import scala.language.implicitConversions
import scala.language.existentials
import scala.reflect.ClassTag

/**
  * Created by johan on 2016-06-12.
  * Modified by Scott 2017-02-06.
  */
case class Entity[T_IdTypes <: IdTypes](id: T_IdTypes#EntityId) {
  def +=[ComponentType](component: ComponentType)(implicit system: System[ComponentType, T_IdTypes]): Entity[T_IdTypes] = {
    system.get(this.id) match {
      case Some(lst) => system.put(this.id, lst :+ component)
      case _ => system.put(this.id, component :: Nil)
    }
    this
  }
  def get[ComponentType](implicit system: System[ComponentType, T_IdTypes]): List[ComponentType] = system.get(this.id).getOrElse(Nil)
  def apply[ComponentType : ClassTag](implicit system: System[ComponentType, T_IdTypes]): List[ComponentType] = {
    val result = system.get(this.id)
    if(result.isEmpty) {
      throw HasNoSuchComponent(id, implicitly[ClassTag[ComponentType]].runtimeClass)
    }
    result.get
  }
  def component[ComponentType : ClassTag](implicit system: System[ComponentType, T_IdTypes]): List[ComponentType] = apply[ComponentType]
  def getComponent[ComponentType](implicit system: System[ComponentType, T_IdTypes]): List[ComponentType] = get[ComponentType]
  def info(implicit ecs: ECS[T_IdTypes]): String = s"Entity-$id { ${ecs.componentsOf(this).mkString(", ")} }"
}

object Entity {

  object Builder {
    def +[ComponentType, T_IdTypes <: IdTypes](component: ComponentType)(implicit system: System[_ >: ComponentType, T_IdTypes]): EntityBuilder[T_IdTypes] = {
      EntityBuilder[T_IdTypes](Seq(PendingComponent(component, system)))
    }
  }

  case class EntityBuilder[T_IdTypes <: IdTypes](pendingComponents: Seq[PendingComponent[_, T_IdTypes]] = new mutable.ArrayBuffer[PendingComponent[_, T_IdTypes]]) {
    def +[ComponentType](component: ComponentType)(implicit system: System[_ >: ComponentType, T_IdTypes]): EntityBuilder[T_IdTypes] = {
      EntityBuilder(pendingComponents :+ PendingComponent(component, system))
    }
    def build(entityId: T_IdTypes#EntityId): Entity[T_IdTypes] = {
      pendingComponents.foreach(_.addToSystem(entityId))
      Entity[T_IdTypes](entityId)
    }
  }

  case class HasNoSuchComponent(entityId: Any, componentType: Class[_])
    extends NoSuchElementException(s"Entity $entityId has no stored component of type ${componentType.getSimpleName}")

  case class PendingComponent[ComponentType, T_IdTypes <: IdTypes](component: ComponentType, system: System[ComponentType, T_IdTypes]){
    def addToSystem(entityId: T_IdTypes#EntityId): Unit = {
      system.get(entityId) match {
        case Some(lst) => system.put(entityId, lst :+ component)
        case _ => system.put(entityId, component :: Nil)
      }
    }
  }

  implicit def entity2Id[T_IdTypes <: IdTypes](entity: Entity[T_IdTypes]): T_IdTypes#EntityId = entity.id

}
