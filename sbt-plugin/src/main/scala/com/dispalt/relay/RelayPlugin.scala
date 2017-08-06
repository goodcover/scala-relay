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

object RelayBasePlugin extends AutoPlugin {

  override def requires = ScalaJSPlugin

  override def trigger = noTrigger

  object autoImport {
    val relaySchema: SettingKey[File] =
      settingKey[File]("Path to schema file")

    val relaySangriaVersion: SettingKey[String] =
      settingKey[String]("Set the Sangria version")

    val relaySangriaCompilerVersion: SettingKey[String] =
      settingKey[String]("Set the Sangria version")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    /**
      * Set the version of the sangria compiler which helps validate GQL
      */
    relaySangriaVersion := "1.2.0",
    /**
      *
      */
    relaySangriaCompilerVersion := "0.2.0",
    /**
      * Runtime dependency on the macro
      */
    libraryDependencies ++= Seq(
      "com.dispalt.relay" %%% "relay-macro" % com.dispalt.relay.core.SRCVersion.current,
      "org.sangria-graphql" %% "sangria" % relaySangriaVersion.value % Provided
    ),
    initialize := {
      val () = sys.props("relay.schema") = relaySchema.value.getAbsolutePath
    }
  )
}


object RelayFilePlugin extends AutoPlugin {

  override def requires =
    ScalaJSPlugin && ScalaJSBundlerPlugin && RelayBasePlugin

  override def trigger = noTrigger

  object autoImport {

    val relayCompile: TaskKey[Unit] = taskKey[Unit]("Run the relay compiler")

    val relayOutput: SettingKey[File] =
      settingKey[File]("Output of the schema stuff")

  }

  import RelayBasePlugin.autoImport._
  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    /**
      * Piggy back on sjs bundler to add our compiler to it.
      */
    npmDevDependencies in Compile ++= Seq(
      "scala-relay-compiler" -> relaySangriaCompilerVersion.value
    ),
    /**
      * Output path of the relay compiler.  Necessary this is an empty directory as it will
      * delete files it thinks went away.
      *
      * **YOU CANNNOT CHANGE THIS SETTING, UNTIL I FIGURE OUT HOW TO PASS SETTINGS TO META GQL**
      *
      */
    relayOutput in Compile := (crossTarget in npmUpdate in Compile).value / "relay-compiler-out",
    /**
      * I don't like this at all but I have no idea how to do this otherwise though...
      *
      * Maybe when this is done, it will work https://github.com/scalameta/scalameta/issues/840
      */
    compile in Compile := {
      (compile in Compile)
        .dependsOn(Def.task[Unit] {
          System.setProperty("relay.out",
                             (relayOutput in Compile).value.getAbsolutePath)
          System.setProperty("relay.schema", relaySchema.value.getAbsolutePath)
        })
        .value
    },
    /**
      * This is used to pass some settings down to a macro, through a old style macro.
      */
    scalacOptions ++= Seq(
      s"-Xmacro-settings:relay.schema=${relaySchema.value.absolutePath}",
      s"-Xmacro-settings:relay.out=${(relayOutput in Compile).value.absolutePath}"
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
