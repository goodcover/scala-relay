import sbt.Keys._
import sbt._

object Versions {
  // Update the .github/workflows/scala.yml with matching Scala versions
  final val Scala212 = "2.12.21"
  // Update the .github/workflows/scala.yml with matching Scala versions
  final val Scala213 = "2.13.18"
  final val Scala3   = "3.3.7"
  // Update the .github/workflows/scala.yml with matching Scala versions
  final val Scala38  = "3.8.1"

  final val Mill = "1.1.2"

  final val Scalajs = org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.scalaJSVersion

  final val Caliban   = "2.11.2"
  final val ScalaMeta = "4.15.2"
  final val UTest     = "0.9.5"
}

object Dependencies {
  val Caliban      = "com.github.ghostdogpr" %% "caliban"   % Versions.Caliban
  val ScalaMeta    = "org.scalameta"         %% "scalameta" % Versions.ScalaMeta exclude ("com.lihaoyi", "sourcecode_2.13")
  def ScalaReflect = Def.setting("org.scala-lang" % "scala-reflect" % scalaVersion.value)

  // Test dependencies
  val UTest       = "com.lihaoyi" %% "utest"        % Versions.UTest
  val millTestkit = "com.lihaoyi" %% "mill-testkit" % Versions.Mill
}
