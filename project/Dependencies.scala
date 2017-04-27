import sbt._
import Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Version {
  final val SjsReact = "1.0.0"
  final val ScalaMeta = "1.7.0"
  final val Scalajs = "0.6.15"
  final val ScalajsBundler = "0.6.0"
}

object Library {
  final val `scalajs-react` = libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % Version.SjsReact
  final val scalameta = "org.scalameta" %% "scalameta" % Version.ScalaMeta
}
