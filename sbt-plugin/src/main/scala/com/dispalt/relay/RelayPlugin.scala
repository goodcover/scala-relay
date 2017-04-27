package com.dispalt.relay

import sbt.{AutoPlugin, SettingKey}
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import sbt.Keys._
import sbt._

object RelayPlugin extends AutoPlugin {

  override def requires = ScalaJSPlugin && ScalaJSBundlerPlugin

  override def trigger = noTrigger

  object autoImport {

    val schemaPath: SettingKey[File] =
      settingKey[File]("Path to schema file")

    val outputPath: SettingKey[File] =
      settingKey[File]("Output of the schema stuff")

    val relayCompile: TaskKey[Unit] = taskKey[Unit]("Run the relay compiler")

  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    outputPath in Compile := (npmUpdate in Compile).value
  )
}
