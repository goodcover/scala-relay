import sbt._
import Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Version {
  final val Scala210 = "2.10.6"
  final val Scala211 = "2.11.11"
  final val Scala212 = "2.12.3"

  final val SjsReact = "1.1.0"
  final val Scalameta = "1.8.0"
  final val Scalajs = "0.6.20"
  final val ScalajsBundler = "0.9.0"
  final val ScalaTest = "3.0.2"
  final val Sangria = "1.2.0"
}

object Library {
  final val `scalajs-react` = libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % Version.SjsReact
  final val scalameta = "org.scalameta" %% "scalameta" % Version.Scalameta
  final val sangria = "org.sangria-graphql" %% "sangria" % Version.Sangria
  final val scalatest = "org.scalatest" %% "scalatest" % Version.ScalaTest % Test
}
