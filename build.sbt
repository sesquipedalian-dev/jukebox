name := "jukebox"
version := "1.0"
scalaVersion := "2.12.1"


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
        "org.json4s" %% "json4s-core" % "3.4.0",
        "org.json4s" %% "json4s-jackson" % "3.4.0"
      )
    )
  )
  .dependsOn(
    scalego_core,
    scalego_serialization
  )
