package com.dispalt.relay

import sbt.{AutoPlugin, SettingKey}
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import sbt.Keys._
import sbt._
import scalajsbundler.util.Commands

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
    outputPath in Compile := (crossTarget in npmUpdate in Compile).value / "jsout",
    /**
      * Piggy back on sjs bundler to add our compiler to it.
      */
    npmDevDependencies in Compile ++= Seq(
      "scala-relay-compiler" -> "0.1.0"
    ),
    /**
      *
      */
    relayCompile := {
      println("RELAYCOMPILE")
      val workingDir = (crossTarget in npmUpdate in Compile).value
      println(workingDir.getAbsolutePath)
      val logger = streams.value.log
      val sp = schemaPath.value
      val source = sourceDirectory.value
      val outpath = (outputPath in Compile).value
      runCompiler(workingDir, sp, source, outpath, logger)
    }
  )

  def runCompiler(workingDir: File,
                  schemaPath: File,
                  sourceDirectory: File,
                  outputPath: File,
                  logger: Logger) = {

    Commands.run(
      Seq(
        "node",
        "./node_modules/scala-relay-compiler/bin/scala-relay-compiler",
        "--schema",
        schemaPath.getAbsolutePath,
        "--src",
        sourceDirectory.getAbsolutePath,
        "--out",
        outputPath.getAbsolutePath
      ),
      workingDir,
      logger
    )
  }
}
