import sbt.Keys._
import sbt._

object Versions {
  final val Scala212 = "2.12.19"
  final val Scala213 = "2.13.14"

  final val Scalajs = org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.scalaJSVersion

  final val Caliban   = "2.8.1"
  final val ScalaMeta = "4.9.8"
}

object Dependencies {
  val Caliban      = "com.github.ghostdogpr" %% "caliban" % Versions.Caliban
  val ScalaMeta    = "org.scalameta" %% "scalameta" % Versions.ScalaMeta
  def ScalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % scalaVersion.value }
}
