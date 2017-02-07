/**
  * Copyright 12/3/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.util.ecs

/**
  * Point2D that's easier for scalego to serialize, with implicit conversions (scalego likes case classes and primitive types)
  */
case class SerializablePoint2D(
  x: Double,
  y: Double
) {
  /**
    * add together vectors
    * @param rhs other vector
    * @return added vector
    */
  def +(rhs: SerializablePoint2D) = SerializablePoint2D(x + rhs.x, y + rhs.y)

  /**
    * @return negated version of this vector (suitable for lhs + -rhs)
    */
  def -() = SerializablePoint2D(-x, -y)

  /**
    * scale the point by rhs
    * @param rhs amount to scale by
    * @return scaled point
    */
  def *(rhs: Double) = SerializablePoint2D(x * rhs, y * rhs)

  /**
   * Rotate point through the angle counterclockwise using matrix math
   * @param angleRadians angle to rotate through, in radians
   * @param counterClockwise if true rotate counterclockwise, otherwise clockwise
   * @return rotated point
   */
  def rotate(angleRadians: Double, counterClockwise: Boolean = true): SerializablePoint2D = {
    // adapted from https://en.wikipedia.org/wiki/Rotation_matrix#In_two_dimensions
    val rotationMatrix = if(counterClockwise) {
      Array(
        Array(Math.cos(angleRadians), -Math.sin(angleRadians)),
        Array(Math.sin(angleRadians), Math.cos(angleRadians))
      )
    } else {
      Array(
        Array(Math.cos(angleRadians), Math.sin(angleRadians)),
        Array(-Math.sin(angleRadians), Math.cos(angleRadians))
      )
    }

    // adapted from https://en.wikipedia.org/wiki/Matrix_multiplication#General_definition_of_the_matrix_product
    val newX = (x * rotationMatrix(0)(0)) + (y * rotationMatrix(0)(1))
    val newY = (x * rotationMatrix(1)(0)) + (y * rotationMatrix(1)(1))

    SerializablePoint2D(newX, newY)
  }
}
