/**
  * Created by Scott on 2/9/2017.
  */
package com.github.sesquipedalian_dev.jukebox.module.snake

import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D

object Direction {
  type DirectionType = String
  val Up = "U"
  val Down = "D"
  val Left = "L"
  val Right = "R"
}

case class TurnStruct(direction: Direction.DirectionType, point: SerializablePoint2D)

case class Player(
  var segments: List[SerializablePoint2D] = Nil,
  var directionOfTravel: Direction.DirectionType = Direction.Up,
  var pendingTail: List[SerializablePoint2D] = Nil,
  var turns: List[TurnStruct] = Nil
) {

  def turnDirection(direction: Direction.DirectionType): Unit = {
    turns = TurnStruct(directionOfTravel, segments.head) :: turns
    directionOfTravel = direction
  }
}
