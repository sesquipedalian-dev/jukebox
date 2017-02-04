/**
  * Copyright 12/2/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine.components.objects

import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D
import com.github.sesquipedalian_dev.util.scalafx.Spritesheet
import com.typesafe.scalalogging.LazyLogging

import scalafx.geometry.Point2D

case class SceneObject(
  var polygon: List[SerializablePoint2D], // first point is the reference point for where to draw texture / animations
  texturePath: Option[String],
  currentAnimation: Option[Spritesheet],
  mouseOverText: Option[String],
  zSort: Int
) extends LazyLogging {
  var isHighlighting: Boolean = false
  var isMousedOver: Boolean = false

  def segments: Iterator[List[Point2D]] = (polygon :+ polygon.head)  // get last segment by looping back to first point
    .sliding(2, 1).map(lst => lst.map(sp2d => new Point2D(sp2d.x, sp2d.y)).sortWith((lhs, rhs) => {
    if(lhs.x == rhs.x) {
      lhs.y < rhs.y
    } else {
      lhs.x < rhs.x
    }
  }))

  def getBoundingBox: List[Point2D] = {
    val sortedPoints = polygon.sortWith((lhs, rhs) => if(lhs.x == rhs.x) { lhs.y < rhs.y } else { lhs.x < rhs.x })
    val topLeft = sortedPoints.head
    val bottomRight = sortedPoints.last
    (new Point2D(topLeft.x, topLeft.y)) :: (new Point2D(bottomRight.x, bottomRight.y)) :: Nil
  }

  // adapted from http://stackoverflow.com/questions/217578/how-can-i-determine-whether-a-2d-point-is-within-a-polygon
  def containsPoint(pointer: Point2D): Boolean = {
    // if not within bounding box, we can rule it out quickly
    val bb = getBoundingBox
    val topLeft = bb.head
    val bottomRight = bb.last
    val outsideBoundingBox =
      pointer.x < topLeft.x ||
      pointer.y < topLeft.y ||
      pointer.x > bottomRight.x ||
      pointer.y > bottomRight.y
    if(outsideBoundingBox) {
      return false
    }

    // if within bounding box, we still might have jagged edges.  Pick a point outside the polygon and raycast to the test point
    val outsidePoint = new Point2D(topLeft.x - 10, topLeft.y - 10)

    val intersectedSides = segments.count(points => {
      val p1 = points.head; val p2 = points.last
      val result = doLineSegmentsIntersect(p1.x, p1.y, p2.x, p2.y, outsidePoint.x, outsidePoint.y, pointer.x, pointer.y)
      logger.trace(s"checking intersection with points {} {} {} {} $result", p1, p2, outsidePoint, pointer)
      result
    })

    // if a ray trace from somewhere outside the polygon to our point intersects an odd number of sides, it's in the polygon
    intersectedSides % 2 == 1
  }

  // adapted from http://stackoverflow.com/questions/217578/how-can-i-determine-whether-a-2d-point-is-within-a-polygon
  // check if two line segments (defined by their start / end x and y coords) intersect
  def doLineSegmentsIntersect(
    v1x1: Double, v1y1: Double, v1x2: Double, v1y2: Double,
    v2x1: Double, v2y1: Double, v2x2: Double, v2y2: Double
  ): Boolean = {
    // Convert vector 1 to a line (line 1) of infinite length.
    // We want the line in linear equation standard form: A*x + B*y + C = 0
    // See: http://en.wikipedia.org/wiki/Linear_equation
    val a1 = v1y2 - v1y1
    val b1 = v1x1 - v1x2
    val c1 = (v1x2 * v1y1) - (v1x1 * v1y2)

    // Every point (x,y), that solves the equation above, is on the line,
    // every point that does not solve it, is not. The equation will have a
    // positive result if it is on one side of the line and a negative one
    // if is on the other side of it. We insert (x1,y1) and (x2,y2) of vector
    // 2 into the equation above.
    val d1 = (a1 * v2x1) + (b1 * v2y1) + c1
    val d2 = (a1 * v2x2) + (b1 * v2y2) + c1

    // If d1 and d2 both have the same sign, they are both on the same side
    // of our line 1 and in that case no intersection is possible. Careful,
    // 0 is a special case, that's why we don't test ">=" and "<=",
    // but "<" and ">".
    logger.trace(s"comparing lines first eq $d1 $d2 $a1 $b1 $c1")
    if (d1 > 0 && d2 > 0) return false
    if (d1 < 0 && d2 < 0) return false

    // The fact that vector 2 intersected the infinite line 1 above doesn't
    // mean it also intersects the vector 1. Vector 1 is only a subset of that
    // infinite line 1, so it may have intersected that line before the vector
    // started or after it ended. To know for sure, we have to repeat the
    // the same test the other way round. We start by calculating the
    // infinite line 2 in linear equation standard form.
    val a2 = v2y2 - v2y1
    val b2 = v2x1 - v2x2
    val c2 = (v2x2 * v2y1) - (v2x1 * v2y2)

    // Calculate d1 and d2 again, this time using points of vector 1.
    val d3 = (a2 * v1x1) + (b2 * v1y1) + c2
    val d4 = (a2 * v1x2) + (b2 * v1y2) + c2

    // Again, if both have the same sign (and neither one is 0),
    // no intersection is possible.
    logger.trace(s"comparing lines second eq $d3 $d4")
    if (d3 > 0 && d4 > 0) return false
    if (d3 < 0 && d4 < 0) return false

    // If we get here, only two possibilities are left. Either the two
    // vectors intersect in exactly one point or they are collinear, which
    // means they intersect in any number of points from zero to infinite.

    // SCA we don't care about the different between COLINEAR and single point in this context
    true

    //if ((a1 * b2) - (a2 * b1) == 0.0f) return COLINEAR
    // If they are not collinear, they must intersect in exactly one point.
  }
}