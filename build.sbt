name := "jukebox"
version := "1.0"
scalaVersion := "2.11.8"

lazy val scalegoSettings = Seq(
  name := "scalego-core",
  organization := "com.github.gigurra",
  version := "0.3.7-SNAPSHOT",
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),
  libraryDependencies ++= Seq(
    "org.scalatest"        %%   "scalatest"             %   "2.2.4"     %   "test",
    "org.mockito"           %   "mockito-core"          %   "1.10.19"   %   "test"
  )
)

lazy val scalego_core = (project in file("scalego-core"))
  .settings(scalegoSettings)

lazy val scalego_serialization = (project in file("scalego-serialization"))
  .settings(
    scalegoSettings ++ Seq(
      name := "scalego-serialization"
    )
  )
  .dependsOn(
    scalego_core
  )

lazy val scalego_serialization_json = (project in file("scalego-serialization-json"))
  .settings(
    scalegoSettings ++ Seq(
      name := "scalego-serialization-json",
      libraryDependencies ++= Seq(
//        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.1.3"
        "org.json4s" %% "json4s-core" % "3.4.0",
        "org.json4s" %% "json4s-jackson" % "3.4.0"
      )
    )
  )
  .dependsOn(
    scalego_core,
    scalego_serialization
  )

lazy val sesquipedalianSettings = Seq(
  organization := "com.github.sesquipedalian_dev",
  version := "1.0",
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")
)

lazy val sesquipedalian_dev_util = (project in file("util"))
  .settings(
    sesquipedalianSettings ++ Seq(
      name := "sesquipedalian-dev-util",
      libraryDependencies ++= Seq(
        "com.typesafe" % "config" % "1.3.1",
        "org.scalafx" %% "scalafx" % "8.0.92-R10",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
        "ch.qos.logback" % "logback-classic" % "1.1.2"

      )
    )
  )
  .dependsOn(
    scalego_core,
    scalego_serialization,
    scalego_serialization_json
  )

lazy val engine_core = (project in file("engine-core"))
  .settings(
    sesquipedalianSettings ++ Seq(
      name := "engine-core",
      libraryDependencies ++= Seq(
        "org.scalafx" %% "scalafx" % "8.0.92-R10",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
        "ch.qos.logback" % "logback-classic" % "1.1.2"
      )
    )
  )
  .dependsOn(
    scalego_core,
    scalego_serialization,
    scalego_serialization_json,
    sesquipedalian_dev_util
  )

lazy val module_asteroids = (project in file("module-asteroids"))
  .settings(
    sesquipedalianSettings ++ Seq(
      name := "module-asteroids"
    )
  )
  .dependsOn(
    engine_core,
    sesquipedalian_dev_util
  )

lazy val module_snake = (project in file("module-snake"))
  .settings(
    sesquipedalianSettings ++ Seq(
      name := "module-snake"
    )
  )
  .dependsOn(
    engine_core,
    sesquipedalian_dev_util
  )