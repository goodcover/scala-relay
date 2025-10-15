import sbt.Keys._
import sbt._

object Versions {
  final val Scala212 = "2.12.20"
  final val Scala213 = "2.13.16"
  final val Scala3   = "3.3.7"
  final val Scala37  = "3.7.2"

  final val Mill = "1.0.6"

  final val Scalajs = org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.scalaJSVersion

  final val Caliban   = "2.11.1"
  final val ScalaMeta = "4.13.10"
  final val UTest     = "0.9.1"
}

object Dependencies {
  val Caliban      = "com.github.ghostdogpr" %% "caliban"   % Versions.Caliban
  val ScalaMeta    = "org.scalameta"         %% "scalameta" % Versions.ScalaMeta exclude ("com.lihaoyi", "sourcecode_2.13")
  def ScalaReflect = Def.setting("org.scala-lang" % "scala-reflect" % scalaVersion.value)

  // Test dependencies
  val UTest       = "com.lihaoyi" %% "utest"        % Versions.UTest
  val millTestkit = "com.lihaoyi" %% "mill-testkit" % Versions.Mill
}
