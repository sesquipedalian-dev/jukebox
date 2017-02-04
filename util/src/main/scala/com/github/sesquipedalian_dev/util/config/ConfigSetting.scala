/**
  * Copyright 11/25/2016 sesquipedalian.dev@gmail.com, All Rights Reserved.
  */
package com.github.sesquipedalian_dev.util.config

import java.io.{BufferedOutputStream, File, FileOutputStream, PrintWriter}

import com.typesafe.config.{Config, ConfigException, ConfigFactory}

import scala.collection.mutable.ListBuffer

// A fancy config setting
// - Observable pattern - users can register to effect change when the setting is updated
// - loaded from config, possibly in some complex way
// - can flag as user editable or no
// - can use default value or no

/**
  * Base trait
  */
trait ConfigSetting[T] {
  // set to true if it can be edited by the user / loaded from the user prefs file
  def userSetting: Boolean = false

  // observable pattern stuff - allow calling classes to get told when we change
  protected val onChangeFuncs: ListBuffer[(Option[T]) => Unit] = ListBuffer()
  def onChange(func: (Option[T]) => Unit): Unit = {
    onChangeFuncs += (func)
  }

  // actual value-related methods
  var value: Option[T] = None
  def defaultValue: T
  def apply(): T = getValue
  def getValue: T = value.getOrElse(defaultValue)
  def getValueOption: Option[T] = value

  // called when config file is telling this setting to parse
  def populateFromConfig(newConfig: Config): Unit

  // register self with global list of all settings
  ConfigSettings._configSettings += this
  ConfigSettings.loadConfig()
}

/**
  * Basic config settings that can be loaded from the text config files with a basic type
  * (e.g. int, string, float, boolean)
  *
  */
trait LoadableConfigSetting[T] extends ConfigSetting[T] {
  // config setting key that we'll look up in the typesafe.Config
  // e.g. application.subsystem.value
  def configFileName: String // TODO we may at some point want multiple aliases - e.g. to support changing the names and still loading old conf files

  // template method for extracting the actual value from the typesafe.Config (e.g. config.GetString)
  protected def configGetMethod(config: Config): T

  // scaffolding for parsing the value from the typesafe.Config
  def populateFromConfig(newConfig: Config): Unit = {
    if(userSetting) { // TODO later, we want to separate user from more authoritative config files
      val oldValue = value
      val newValue = try {
        Some(configGetMethod(newConfig))
      } catch {
        case _: ConfigException => {
          None
        }
      }
      value = newValue
      onChangeFuncs.foreach(_(newValue))
    }
  }
}
trait LoadableStringConfigSetting extends LoadableConfigSetting[String] {
  def configGetMethod(config: Config): String = config.getString(configFileName)
}
trait LoadableIntConfigSetting extends LoadableConfigSetting[Int] {
  def configGetMethod(config: Config): Int = config.getInt(configFileName)
}
trait LoadableBooleanConfigSetting extends LoadableConfigSetting[Boolean] {
  def configGetMethod(config: Config): Boolean = config.getBoolean(configFileName)
}
trait LoadableDoubleConfigSetting extends LoadableConfigSetting[Double] {
  def configGetMethod(config: Config): Double = config.getDouble(configFileName)
}

/*
 * singleton class to tell all config settings to load from file
 */
object ConfigSettings {
  // TODO generalize for other OS; make user-editable config setting
  val USER_PREFS_LOC = System.getenv("AppData") + "/Jukebox/application.conf"

  val _configSettings: ListBuffer[ConfigSetting[_]] = ListBuffer()
  def configSettings = _configSettings.toList

  private def getFile: File = {
    val userConfFile: File = new File(USER_PREFS_LOC)
    if(!userConfFile.exists) {
      userConfFile.getParentFile.mkdirs()
      userConfFile.createNewFile()
    }
    userConfFile
  }

  def loadConfig(): Unit = {
    // this is the base typesafe-config way of loading - pulls from
    // any config file on the classpath, and that isn't safe with the idea we have of 'user editable' config
    //val config = ConfigFactory.load()

    // TODO we want to separate user config from authoritative config
    val userConfig = ConfigFactory.parseFile(getFile)
    _configSettings.foreach(_.populateFromConfig(userConfig))
  }

  def saveUserConfig(): Unit = {
    val file = getFile
    val outStream = new PrintWriter(new FileOutputStream(file))

    _configSettings foreach {
      case x: LoadableConfigSetting[_] if x.userSetting => {
        x.getValueOption.foreach(v => {
          val escaped = "\"" + v.toString.replace("\"", "\"\"").replace("\\", "\\\\") + "\""
          outStream.println(x.configFileName + "=" + escaped)
        })
      }
      case _ =>
    }
    outStream.close()

    // then reload
    loadConfig()
  }
}