package com.dispalt.relay

import sbt.{AutoPlugin, SettingKey}
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbt.inc.Analysis

import scalajsbundler.util.Commands

object RelayPlugin extends AutoPlugin {

  override def requires = ScalaJSPlugin && ScalaJSBundlerPlugin

  override def trigger = noTrigger

  object autoImport {

    val relaySchema: SettingKey[File] =
      settingKey[File]("Path to schema file")

    val relayOutput: SettingKey[File] =
      settingKey[File]("Output of the schema stuff")

    val relayCompile: TaskKey[Unit] = taskKey[Unit]("Run the relay compiler")

    val relaySangriaVersion: SettingKey[String] =
      settingKey[String]("Set the Sangria version")

    val relaySangriaCompilerVersion: SettingKey[String] =
      settingKey[String]("Set the Sangria version")

    val relay: TaskKey[String] = taskKey[String]("The relay task")

  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    /**
      * Output path of the relay compiler.  Necessary this is an empty directory as it will
      * delete files it thinks went away.
      *
      */
    relayOutput in Compile := (crossTarget in npmUpdate in Compile).value / "relay-compiler-out",
    /**
      * Piggy back on sjs bundler to add our compiler to it.
      */
    npmDevDependencies in Compile ++= Seq(
      "scala-relay-compiler" -> relaySangriaCompilerVersion.value
    ),
    /**
      * Set the version of the sangria compiler which helps validate GQL
      */
    relaySangriaVersion := "1.2.0",
    /**
      *
      */
    relaySangriaCompilerVersion := "0.1.3",
    /**
      * I don't like this at all but I have no idea how to do this otherwise though...
      */
    initialize := {
      val () = sys.props("relay.out") =
        (relayOutput in Compile).value.getAbsolutePath
      val () = sys.props("relay.node_modules") =
        (target in relay).value.getAbsolutePath
      val () = sys.props("relay.schema") = relaySchema.value.getAbsolutePath
      initialize.value
    },
    /**
      *
      * Part of the magic is the interaction this plugin has with the macro.
      */
    scalacOptions ++= Seq(
      s"-Drelay.out=${(relayOutput in Compile).value.getAbsolutePath}",
      s"-Drelay.schema=${relaySchema.value.getAbsolutePath}"
    ),
    /**
      * Runtime dependency on the macro
      */
    libraryDependencies ++= Seq(
      "com.dispalt.relay" %%% "relay-macro" % com.dispalt.relay.core.SRCVersion.current,
      "org.sangria-graphql" %% "sangria" % relaySangriaVersion.value % Provided
    ),
    /**
      * Actually compile relay, don't overwrite this.
      */
    relayCompile := {
      val workingDir = (crossTarget in npmUpdate in Compile).value

      /* This actually does the yarn/npm update */
      (npmUpdate in Compile).value
      val logger = streams.value.log
      val sp = relaySchema.value
      val source = sourceDirectory.value
      val outpath = (relayOutput in Compile).value
      runCompiler(workingDir, sp, source, outpath, logger)
    },
    target in relay := {
      val dir = target.value / "relay-compiler"
      IO.createDirectory(dir)
      IO.createDirectory(dir / "out")
      dir
    },
    relay := {
      import sys.process._
      val v = relaySangriaCompilerVersion.value
      val cwd = (target in relay).value
//      IO.createDirectory(cwd)
      val logger = streams.value.log
      if (!(cwd / "yarn.lock").exists()) {
        logger.info(
          s"Rerunning yarn install scala-relay-compiler, missing this file, ${cwd / "yarn.lock"}")
        Process(s"yarn add scala-relay-compiler@$v", cwd)
          .!(ProcessLogger(o => logger.info(o), e => logger.error(e)))
      }
      ""
    },
    compile in Compile := (compile in Compile).dependsOn(relay).value,
    /**
      * Rewire the webpack task to depend on compiling relay
      */
    webpack in fastOptJS in Compile := (webpack in fastOptJS in Compile)
      .dependsOn(relayCompile)
      .value
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
