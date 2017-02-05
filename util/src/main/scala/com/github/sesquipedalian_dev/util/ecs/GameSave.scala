/**
  * Copyright 11/28/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.util.ecs

import java.io._

import com.github.gigurra.scalego.core.{ECS, IdTypes}
import com.github.gigurra.scalego.serialization.{IdTypeMapper, KnownSubTypes}
import com.github.gigurra.scalego.serialization.json.JsonSerializer
import com.github.sesquipedalian_dev.util.config.LoadableStringConfigSetting
import com.github.sesquipedalian_dev.util.scalafx.StringConfigSettingWithUI
import com.typesafe.scalalogging.LazyLogging

case object USER_SAVES_LOC extends LoadableStringConfigSetting with StringConfigSettingWithUI {
  override def configFileName: String = "jukebox.saves.userSavesDir"
  override def defaultValue: String = System.getenv("AppData") + "/Jukebox/saves" // TODO generalize for other OS?
  override def userSetting: Boolean = true

  override def validateString(tryValue: String): Boolean = {
    val savesDir = new File(tryValue)
    if (!savesDir.exists) {
      savesDir.mkdirs()
    } else {
      true
    }
  }
}

// utility to assist with saving / restoring game states
// based on the scalego Entity-Component System
object GameSave extends LazyLogging {
  def getSavesDir(): File = {
    val f = new File(USER_SAVES_LOC.getValue)
    if(!f.exists()) {
      f.mkdirs()
    }
    f
  }

  /**
    * load a game save json to make a new ECS
    * @param filename save file to load
    * @throws java.io.IOException
    */
  def loadGame[ECSIdType <: IdTypes]
    (filename: String, systems: List[com.github.gigurra.scalego.core.System[_, ECSIdType]])
    (implicit EidTypeMapper: IdTypeMapper[ECSIdType#EntityId],
     SystemTypeMapper: IdTypeMapper[ECSIdType#SystemId],
     knownSubtypes: KnownSubTypes
  ): (String, ECS[ECSIdType]) = {
    val savesDir = getSavesDir()
    val saveFile = new File(savesDir, filename)

    // load in the file
    // adapted from http://stackoverflow.com/questions/4716503/reading-a-plain-text-file-in-java
    val inputReader = new BufferedReader(new FileReader(saveFile))
    val sb = new StringBuilder
    var line = inputReader.readLine()
    var first = true
    var moduleName: String = ""
    while(line != null) {
      if(first) {
        moduleName = line
        first = false
      }
      sb.append(line)
      sb.append(System.lineSeparator())
      line = inputReader.readLine()
    }
    inputReader.close()

    // make serializer object
    val serializer = JsonSerializer[ECSIdType](knownSubtypes)

    // installs .toJson method on ecs
    import serializer._

    // do the parse
    val jsonToParse = sb.toString
    val newEcs = ECS[ECSIdType](systems:_*)
    val pretty = newEcs.toJson(pretty = true)
    logger.info("Pretty JSON B4: " + pretty)
    newEcs.appendJson(jsonToParse)
    val pretty2 = newEcs.toJson(pretty = true)
    logger.info("Pretty JSON Afta: " + pretty2)

    (moduleName, newEcs)
  }

  /**
    * save the given game state; returns false if file already existed and we don't have shouldOverwrite
    * @param ecs ecs to save / serialize
    * @param filename logical file name for this save; doesn't need to be a system file name but just a unique
    *                 descriptor for that save record
    * @param shouldOverwrite true if user confirmed overwriting existing file
    * @return true if we would up writing the file
    * @throws java.io.IOException
    */
  def saveGame[ECSIdType <: IdTypes]
    (ecs: ECS[ECSIdType], filename: String, moduleName: String, shouldOverwrite: Boolean = false)
    (implicit EidTypeMapper: IdTypeMapper[ECSIdType#EntityId], SystemTypeMapper: IdTypeMapper[ECSIdType#SystemId], knownSubtypes: KnownSubTypes): Boolean =
  {
    val savesDir = getSavesDir
    val saveFile = new File(savesDir, filename)
    if(!saveFile.exists) {
      saveFile.createNewFile()
    } else if (!shouldOverwrite) {
      return false
    }

    // make serializer object
    val serializer = JsonSerializer[ECSIdType](knownSubtypes)

    // installs .toJson method on ecs
    import serializer._

    // pretty-print json ala jsonlint.com
    val pretty = ecs.toJson(pretty = true)
    logger.info("Pretty JSON: " + pretty)

    val outStream = new PrintWriter(new FileOutputStream(saveFile))
    outStream.print(moduleName + "\n") // TODO
    outStream.print(pretty)
    outStream.close

    true
  }
}
