package com.dispalt.relay

import sbt.Keys._
import sbt._

object RelayPlugin extends AutoPlugin {

  override def requires: Plugins = RelayBasePlugin

  override def trigger = noTrigger

  import RelayBasePlugin.autoImport._

  override lazy val projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(perConfigSettings)

  def perConfigSettings: Seq[Setting[_]] =
    Seq(
      sourceGenerators += relayConvert.taskValue.map(_.toSeq),
      resourceGenerators += relayCompile.taskValue.map(_.toSeq)
    )
}
