import sbt._
import Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Version {
  final val Scala212 = "2.12.15"
  final val Scala213 = "2.13.8"

  final val Slinky  = "0.7.2"
  final val Scalajs = org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.scalaJSVersion

  final val ScalaTest = "3.2.12"

}

object Library {
  final val scalatest = "org.scalatest"                    %% "scalatest"    % Version.ScalaTest % Test
  final val slinky    = libraryDependencies += "me.shadaj" %%% "slinky-core" % Version.Slinky
}
