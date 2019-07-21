import sbt._
import Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Version {
  final val Scala211 = "2.11.11"
  final val Scala212 = "2.12.8"

  final val SjsReact       = "1.1.0"
  final val Scalajs        = org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport.scalaJSVersion
  final val ScalajsBundler = "0.15.0"
  final val ScalaTest      = "3.0.8"
  final val Sangria        = "1.4.2"

}

object Library {
  final val `scalajs-react` = libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core"     % Version.SjsReact
  final val sangria         = "org.sangria-graphql"                                      %% "sangria"   % Version.Sangria
  final val scalatest       = "org.scalatest"                                            %% "scalatest" % Version.ScalaTest % Test
}
