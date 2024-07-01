package com.dispalt.relay

import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt.Keys.*
import sbt.{AutoPlugin, *}

object RelayGeneratePlugin extends AutoPlugin {

  override def requires: Plugins =
    ScalaJSPlugin && RelayBasePlugin

  override def trigger = noTrigger

  import RelayBasePlugin.autoImport.*

  override lazy val projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(perConfigSettings)

  def perConfigSettings: Seq[Setting[_]] =
    Seq(
      /**
        * Hook the relay compiler into the compile pipeline.
        */
      sourceGenerators += relayConvert.taskValue.map(_.toSeq),
      resourceGenerators += relayCompile.taskValue.map(_.toSeq)
    )
}
