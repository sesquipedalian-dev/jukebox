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
