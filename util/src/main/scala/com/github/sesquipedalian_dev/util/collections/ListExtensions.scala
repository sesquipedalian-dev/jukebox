/**
  * Copyright 2/7/2017 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.util.collections

object ListExtensions {
  def cartesianProduct[A, B](lhs: List[A], rhs: List[B]): List[(A, B)] = {
    for {
      a <- lhs
      b <- rhs
    } yield {
      (a, b)
    }
  }
}
