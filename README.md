# Project overview
Jukebox is a small game engine in Scala based on the JavaFX graphics / UI library.  It loads different game modules from separate jars.

# Motivation
This is a small learning project for me. I wanted to try and make a small 2D game engine in my most recently fluent language (Scala).

# How to Run
1. Grab a [Java](https://java.com/en/download/) version greater than 1.8 Update 40 (when newest javafx library was added)
2. Clone repo:
  * download zip: https://github.com/sesquipedalian-dev/jukebox/archive/master.zip 
  * svn: svn checkout https://github.com/sesquipedalian-dev/jukebox.git <target directory> 
  * git: git clone https://github.com/sesquipedalian-dev/jukebox.git <target directory> 
3. Run game from 'out' directory':
  * Windows: double-click jukebox.bat to get the command line right for you 
  * command line start: java -cp jukebox.jar;libs/* com.github.sesquipedalian_dev.jukebox.engine.Main 
4. Pick a module to play from the 'Modules' menu

# Repository Components

## engine-core
Game engine core - main game loop, main graphics window, entity-component system, etc

## jukebox
loads modules into tycoon-core to run various little games

## util
utility classes that I could see using in other projects

## module-asteroids
jukebox module to play a variant of the classic game Asteroids

## scalego stuff
I pulled this library into the repo to adjust it so that an Entity can have multiple Components within the same System.  Example: I wanted a 'renderer' system that gets called to draw a thing every graphics frame;  an entity could have both a sprite renderer and a special effects / vfx renderer on it.

# Library Use
* scalego - An Entity Component System implementation in Scala
* scalafx - Scala wrapper library for JavaFX - built-in java functionality for UI / Graphics
* logback - logging
* typesafe logging - logging
* typesafe config - config file format / loading

# Extending / Building
IntelliJ IDEA / JetBrains IDE project files included in repo.  Includes test run configurations for the scalego library, but for running the main application the preferred method is to run the project build in the IDE, then run the game from the 'out' directory.
