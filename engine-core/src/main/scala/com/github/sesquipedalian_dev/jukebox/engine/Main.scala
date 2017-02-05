/**
  * Copyright 11/24/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.jukebox.engine

import javafx.scene.canvas.Canvas

import com.github.sesquipedalian_dev.jukebox.engine.ui.I18nManager
import com.github.sesquipedalian_dev.util.config._
import com.github.sesquipedalian_dev.util.scalafx.{ConfigSettingWithUI, ConfigSettingWithUIController}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scalafx.animation.{KeyFrame, Timeline}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.ComboBox

object Main extends JFXApp with LazyLogging {
  // FOR TESTING fail on unknown locale
//  I18nManager.setLocale(new Locale("es", "ES"))
  // FOR TESTING

  /** ******************************
    * load up our UI from fxml
    */
  val fxmlFile = getClass.getResource("/fxml/main.fxml")
  val loader = new javafx.fxml.FXMLLoader()
  loader.setResources(I18nManager.currentLocBundle)
  val root: javafx.scene.Parent = loader.load(fxmlFile.openStream())
  val theScene = new javafx.scene.Scene(root, WINDOW_WIDTH(), WINDOW_HEIGHT())
  val scalaFxScene = new Scene(theScene)
  stage = new PrimaryStage {
    title = "ScalaFX Hello World"
    scene = scalaFxScene
    resizable = false
  }

  // set up window size config settings to be changeable in the preferences
  SCREEN_RESOLUTION() // make sure this loads
  WINDOW_WIDTH.onChange(newValue => {
    if(newValue.nonEmpty) {
      stage.setWidth(newValue.get)
      fixCanvasSizeToWindow()
    }
  })
  WINDOW_HEIGHT.onChange(newValue => {
    if(newValue.nonEmpty) {
      stage.setHeight(newValue.get)
      fixCanvasSizeToWindow()
    }
  })

  /*******************************
    * Init GameLoop
    * Adapted from: https://gamedevelopment.tutsplus.com/tutorials/introduction-to-javafx-for-game-development--cms-23835 // 'The Game Loop'
    */
  val gameLoop = new GameLoop()

  val gameLoopTimeline = new Timeline()
  val kf = new javafx.animation.KeyFrame(
    javafx.util.Duration.millis(MS_PER_UPDATE() / 1000000),
    gameLoop
  )
  gameLoopTimeline.keyFrames = List[KeyFrame](
    new KeyFrame(kf)
  )
  gameLoopTimeline.cycleCount = javafx.animation.Animation.INDEFINITE
  gameLoopTimeline.play()

  def fixCanvasSizeToWindow(): Unit = {
    // make canvas resize / scale correct - we want to render to a constant resolution,
    // and then have it stretched to fit the user's selected screen size
    theScene.lookup("#canvas") match {
      case canvas: Canvas => {
        val stageHeight = stage.height() - 25.0 /* padding for menu */
        val stageWidth = stage.width()
        canvas.setWidth(CANVAS_WIDTH.getValue)
        canvas.setHeight(CANVAS_HEIGHT.getValue)

        val xScale = stageWidth / CANVAS_WIDTH.getValue
        val yScale = stageHeight / CANVAS_HEIGHT.getValue

        // NOTE: for this to work, we had to put the canvas inside a stack pane that takes up the whole
        // space where the canvas would go.  This keeps the canvas centered in the pane while it scales.
        // see http://stackoverflow.com/questions/16680295/javafx-correct-scaling
        canvas.setScaleX(xScale)
        canvas.setScaleY(yScale)
      }
      case x => logger.error("Couldn't find canvas {}", x)
    }
  }

  def apply() {
    throw new Exception("testing what this gets compiled to?")
  }
  this()
}
