/**
  * Copyright 12/3/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.util.scalafx

import java.awt.image.BufferedImage
import java.io.{File, FileInputStream}
import javax.imageio.ImageIO

import com.github.sesquipedalian_dev.util.ecs.SerializablePoint2D

import scalafx.scene.image.Image
import com.typesafe.scalalogging.LazyLogging

/**
  * Custom class to handle loading a 2D animation from a spritesheet (file where frames of the anim are packed together
  * at different x/y coordinates).  Surely there's a built-in way to do this, but I couldn't find it with the
  * various libraries we're tying together (e.g. scalafx, javafx). Particular improvements:
  *
  * - cached loading - if multiple objects are using same spritesheet, they shouldn't need multiple copies in memory
  * - using graphics context to rewrite chunks of the image can't be efficient
  *
  * @param resourcePath should be the name, ending in '*.ext';  assumed to be a resource we can load;
  *                     let's assume that the result parts are ordered from top to bottom and left to right
  * @param startingPoint x/y coord to start splitting
  * @param paddingBetweenChunks x / y padding between chunks
  * @param chunkSize width / height of chunk
  * @param framesPerImg how many frames should a single anim frame take?
  */
case class Spritesheet(
  resourcePath: String,
  startingPoint: SerializablePoint2D,
  paddingBetweenChunks: SerializablePoint2D,
  chunkSize: SerializablePoint2D,
  framesPerImg: Int
) extends LazyLogging {

  // TODO generalize for other OS; make user-editable config setting
  val SPLIT_SPRITES_LOC = System.getenv("AppData") + "/Jukebox/sprites"

  def getParts(): List[Image] = {
    // parse out file name info from the source path
    val parts1 = resourcePath.split("/")
    val parts = parts1.last.split("\\.")
    val withoutExt = parts.reverse.tail.reverse.mkString(".")
    val ext = parts.last
    val dir = SPLIT_SPRITES_LOC + "/" + withoutExt
    val dirFile = new File(dir)
    if(!dirFile.exists()) {
      dirFile.mkdirs()
    }

    // adapted from http://kalanir.blogspot.com/2010/02/how-to-split-image-into-chunks-java.html
    // I've modified it to handle fixed size info instead of a fixed rows / columns,
    // and obviously to port it to Scala
    val originalImgFile = getClass().getResource(resourcePath).getFile
    val fis = new FileInputStream(originalImgFile)
    val imageBuffer = ImageIO.read(fis)

    val chunkHeight = chunkSize.y + paddingBetweenChunks.y
    val rows = (imageBuffer.getHeight() - startingPoint.y) / chunkHeight
    val chunkWidth = chunkSize.x + paddingBetweenChunks.x
    val cols = (imageBuffer.getWidth() - startingPoint.x) / chunkWidth

    logger.debug(s"Spritesheet load: {$chunkHeight} {$rows} {$chunkWidth} {$cols} ${imageBuffer.getHeight} ${imageBuffer.getWidth}")
    var images: List[Image] = Nil

    for {
      currentCol <- 0 until cols.toInt // cols then rows gives desired order in list; left to right then top to bottom
      currentRow <- 0 until rows.toInt
    } {
      val targetFilename = new File(dirFile, withoutExt + "_" + currentRow + "_" + currentCol + "." + ext)

      if(!targetFilename.exists) {
        val img = new BufferedImage(chunkWidth.toInt - paddingBetweenChunks.x.toInt, chunkHeight.toInt - paddingBetweenChunks.y.toInt, imageBuffer.getType)

        val gr = img.createGraphics()
        val d1x = 0; val d1y = 0
        val d2x = chunkWidth.toInt - paddingBetweenChunks.x.toInt; val d2y = chunkHeight.toInt - paddingBetweenChunks.y.toInt
        val s1x = (chunkWidth.toInt * currentCol.toInt) + startingPoint.x.toInt
        val s1y = (chunkHeight.toInt * currentRow.toInt) + startingPoint.y.toInt
        val s2x = s1x + chunkWidth.toInt - paddingBetweenChunks.x.toInt
        val s2y = s1y + chunkHeight.toInt - paddingBetweenChunks.y.toInt

        logger.debug(s"drawing chunk from orig img: ${s1x} $s1y $s2x $s2y")

        gr.drawImage(
          imageBuffer,
          d1x, d1y, d2x, d2y, s1x, s1y, s2x, s2y,
          null
        )
        gr.dispose()

        ImageIO.write(img, ext, targetFilename)
      }
      logger.debug(s"telling it to load images ${"file://" + targetFilename.getPath}")
      images = images :+ (new Image(targetFilename.toURI().toString)) // toURI suggested by: http://stackoverflow.com/questions/16099427/cannot-load-image-in-javafx
    }

    images
  }
}
