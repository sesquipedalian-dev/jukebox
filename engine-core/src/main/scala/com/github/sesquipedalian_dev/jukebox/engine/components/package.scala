/**
  * Copyright 2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine

import java.util.UUID

import com.github.gigurra.scalego.core.IdTypes
import com.github.gigurra.scalego.serialization.{IdTypeMapper, KnownSubTypes}

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
  implicit val knownSubtypes: KnownSubTypes =
    gameloop.knownSubtypes +
    objects.knownSubtypes

  // generate a blank set of ECS systems for the application.  The returned callback puts the systems into the
  // associated implicit vars (finalizes the operation).
  // TODO - this should be structured more so that the sub coimponents implement some interface
  // to get their blank ECS systems, then they are registered here to loop through them all and get the
  // stuff we need
  def blankSystemsSet: (() => Unit, List[com.github.gigurra.scalego.core.System[_, UUIDIdType]]) = {
    val (gameCb, gameLst) = objects.blankSystemsSet
    val (frameworkCb, frameworkLst) = gameloop.blankSystemsSet

    val cb = () => {
      gameCb()
      frameworkCb()
    }

    (cb, gameLst ++ frameworkLst)
  }
}
