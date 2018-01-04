package com.dispalt.relay

import sbt.{AutoPlugin, SettingKey}
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

import sbt.Keys._
import sbt._

import scalajsbundler.util.Commands

object RelayBasePlugin extends AutoPlugin {

  override def requires = ScalaJSPlugin

  override def trigger = noTrigger

  object autoImport {
    val relaySchema: SettingKey[File]                   = settingKey[File]("Path to schema file")
    val relayValidateQuery: SettingKey[Boolean]         = settingKey[Boolean]("Validate queries in macro")
    val relaySangriaVersion: SettingKey[String]         = settingKey[String]("Set the Sangria version")
    val relaySangriaCompilerVersion: SettingKey[String] = settingKey[String]("Set the Sangria version")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
        /**
          * Set the version of the sangria compiler which helps validate GQL
          */
        relaySangriaVersion := "1.3.1",
        /**
          *
          */
        relaySangriaCompilerVersion := "0.6.10",
        /**
          * Runtime dependency on the macro
          */
        libraryDependencies ++= Seq("com.dispalt.relay"   %%% "relay-macro" % com.dispalt.relay.core.SRCVersion.current,
                                    "org.sangria-graphql" %% "sangria"      % relaySangriaVersion.value % Provided),
        /**
          * Should we validate queries?
          */
        relayValidateQuery := false)
}

object RelayFilePlugin extends AutoPlugin {

  override def requires =
    ScalaJSPlugin && RelayBasePlugin

  override def trigger = noTrigger

  object autoImport {

    val relayCompile: TaskKey[Seq[File]] = taskKey[Seq[File]]("Run the relay compiler")
    val relayOutput: SettingKey[File]    = settingKey[File]("Output of the schema stuff")
    val relayCompilerPath: SettingKey[String] =
      settingKey[String]("The location of the `scala-relay-compiler` executable.")
  }

  import RelayBasePlugin.autoImport._
  import autoImport._

  val relayFolder = "relay-compiler-out"

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
        /**
          * Add the path so the macro can find the schema.
          */
        scalacOptions += s"-Xmacro-settings:relaySchema=${relaySchema.value.absolutePath}",
        /**
          * If set to validate, then pass the setting again to the macro.
          */
        scalacOptions ++= (if (relayValidateQuery.value) Seq(s"-Xmacro-settings:relayValidateQuery=true") else Seq()),
        /**
          * Output path of the relay compiler.  Necessary this is an empty directory as it will
          * delete files it thinks went away.
          */
        relayOutput in Compile := (sourceManaged in Compile).value / relayFolder,
        /**
          *
          */
        relayCompilerPath := {
          "scala-relay-compiler"
        },
        /**
          * Meat of the function.
          */
        relayCompile in Compile := Def
          .task[Seq[File]] {
            val cache        = streams.value.cacheDirectory / "relay-compile"
            val sourceFiles  = (unmanagedSourceDirectories in Compile).value
            val outpath      = (relayOutput in Compile).value
            val compilerPath = relayCompilerPath.value

            IO.createDirectory(outpath)

            // Filter based on the presence of the annotation.
            val scalaFiles =
              (sourceFiles ** "*.scala").get
                .filter(f => IO.read(f).contains("@gql"))
                .toSet
            val label      = Reference.display(thisProjectRef.value)
            val workingDir = file(sys.props("user.dir"))
            val logger     = streams.value.log
            val sp         = relaySchema.value
            val source     = sourceDirectory.value

            sbt.shim.SbtCompat.FileFunction
              .cached(cache)(FilesInfo.hash, FilesInfo.exists)(
                handleUpdate(label, workingDir, compilerPath, sp, source, outpath, logger))(scalaFiles)
              .toSeq
          }
          .value,
        /**
          * Hook the relay compiler into the compile pipeline.
          */
        sourceGenerators in Compile += (relayCompile in Compile).taskValue)

  implicit class QuoteStr(s: String) {
    def quote: String = "\"" + s + "\""
  }

  def runCompiler(workingDir: File,
                  compilerPath: String,
                  schemaPath: File,
                  sourceDirectory: File,
                  outputPath: File,
                  logger: Logger): Unit = {

    // TODO: this sucks not sure how to get npm scripts to work from java PB.
    val shell = if (System.getProperty("os.name").toLowerCase().contains("win")) {
      List("cmd.exe", "/C")
    } else List("sh", "-c")

    val cmd = shell :+ List(compilerPath,
                            "--schema",
                            schemaPath.getAbsolutePath.quote,
                            "--src",
                            sourceDirectory.getAbsolutePath.quote,
                            "--out",
                            outputPath.getAbsolutePath.quote).mkString(" ")

    Commands.run(cmd, workingDir, logger)
  }

  def handleUpdate(label: String,
                   workingDir: File,
                   compilerPath: String,
                   schemaPath: File,
                   sourceDirectory: File,
                   outputPath: File,
                   logger: Logger)(in: ChangeReport[File], out: ChangeReport[File]): Set[File] = {
    val files = in.modified -- in.removed
    sbt.shim.SbtCompat.Analysis
      .counted("Scala source", "", "s", files.size)
      .foreach { count =>
        logger.info(s"Executing relayCompile on $count $label...")
        runCompiler(workingDir, compilerPath, schemaPath, sourceDirectory, outputPath, logger)
        logger.info(s"Finished relayCompile.")
      }
    files
  }
}
