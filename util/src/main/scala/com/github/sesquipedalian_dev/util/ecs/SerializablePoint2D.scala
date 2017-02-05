/**
  * Copyright 12/3/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.util.ecs

// Point2D that's easier for scalego to serialize, with implicit conversions (scalego likes primitive types)
case class SerializablePoint2D(
  x: Double,
  y: Double
) {
  def +(rhs: SerializablePoint2D) = SerializablePoint2D(x + rhs.x, y + rhs.y)
  def *(rhs: Double) = SerializablePoint2D(x * rhs, y * rhs)
}
