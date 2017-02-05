/**
  * Copyright 2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine

import java.util.UUID

import com.github.gigurra.scalego.core.IdTypes
import com.github.gigurra.scalego.serialization.{IdTypeMapper, KnownSubTypes}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class UUIDIdType extends IdTypes {
  type SystemId = String
  type EntityId = components.EntityIdType
}

package object components {
  type EntityIdType = java.util.UUID
  def randomEntityID: EntityIdType = UUID.randomUUID()

  // type mapper required for using IDs other than String or Long
  implicit val UUIDTypeMapper = new IdTypeMapper[java.util.UUID] {
    override def intermediary2Id(id: String): UUID = java.util.UUID.fromString(id)
    override def id2Intermediary(id: UUID): String = id.toString()
  }

  // mapper required for serialization
  implicit var knownSubtypes: KnownSubTypes = KnownSubTypes()

  // generate a blank set of ECS systems for the application.  The returned callback puts the systems into the
  // associated implicit vars (finalizes the operation).
  def makeNewECSSystems(): (() => Unit, List[com.github.gigurra.scalego.core.System[_, UUIDIdType]]) = {
    val componentToList = componentModules.toList.map(module => {
      val systems = module.makeSystems()
      module -> systems
    }).toMap

    val cb = () => {
      componentToList.foreach(p => {
        val (component, systemsMade) = p
        component.systemsMadeCallback(systemsMade)
      })

    }

    (cb, componentToList.flatMap(_._2).toList)
  }

  val componentModules: mutable.Set[ComponentModule] = mutable.HashSet()

  private def updateSubtypes(): Unit = {
    knownSubtypes = componentModules.foldLeft(KnownSubTypes())((soFar, next) => soFar + next.subtypes)
  }

  def addComponent(module: ComponentModule): Unit = {
    componentModules.add(module)
    updateSubtypes()
  }

  def clearComponents(): Unit = {
    componentModules.clear()
    updateSubtypes()
  }

  def rmComponent(module: ComponentModule): Unit = {
    componentModules.remove(module)
    updateSubtypes()
  }
}
