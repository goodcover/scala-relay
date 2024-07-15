package com.goodcover.relay

import sbt.Keys._
import sbt._

object ScalaRelayPlugin extends AutoPlugin {

  override def requires: Plugins = ScalaRelayBasePlugin

  import ScalaRelayBasePlugin.autoImport._

  override lazy val projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(perConfigSettings)

  def perConfigSettings: Seq[Setting[_]] =
    Seq(
      sourceGenerators += relayConvert.taskValue.map(_.toSeq),
      resourceGenerators += relayCompile.taskValue.map(_.toSeq)
    )
}
