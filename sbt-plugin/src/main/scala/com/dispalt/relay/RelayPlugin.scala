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
    val relaySchema: SettingKey[File]                   = settingKey[File]("Path to schema file")
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
        relaySangriaCompilerVersion := "0.6.3",
        /**
          * Runtime dependency on the macro
          */
        libraryDependencies ++= Seq("com.dispalt.relay"   %%% "relay-macro" % com.dispalt.relay.core.SRCVersion.current,
                                    "org.sangria-graphql" %% "sangria"      % relaySangriaVersion.value % Provided))
}

object RelayFilePlugin extends AutoPlugin {

  override def requires =
    ScalaJSPlugin && ScalaJSBundlerPlugin && RelayBasePlugin

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
//        /**
//          * Piggy back on sjs bundler to add our compiler to it.
//          */
//        npmDevDependencies in Compile ++= Seq("scala-relay-compiler" -> relaySangriaCompilerVersion.value),
        /**
          * Output path of the relay compiler.  Necessary this is an empty directory as it will
          * delete files it thinks went away.
          *
          *
          */
        relayOutput in Compile := (sourceManaged in Compile).value / relayFolder,
        /**
          *
          */
        relayCompilerPath := {
          "scala-relay-compiler"
        },
        scalaJSNativeLibraries in Compile := Attributed.blank(Seq.empty),
        scalaJSNativeLibraries in Test := Attributed.blank(Seq.empty),
        //        compile in Compile := {
        //          (npmUpdate in Compile).value
        //          relayCompile.value
        //          (compile in Compile).value
        //        },
        /**
          * Meat of the function.
          */
        relayCompile in Compile := Def
          .taskDyn[Seq[File]] {
            val log          = streams.value.log
            val cache        = streams.value.cacheDirectory / "relay-compile"
            val sourceFiles  = (unmanagedSourceDirectories in Compile).value
            val outpath      = (relayOutput in Compile).value
            val compilerPath = relayCompilerPath.value

            IO.createDirectory(outpath)

            val scalaFiles =
              (sourceFiles ** "*.scala").get
                .filter(f => IO.read(f).contains("@gql"))
                .toSet
            val label = Reference.display(thisProjectRef.value)
            // TODO: fix mutability
            var ran = false

            def handleUpdate(in: ChangeReport[File], out: ChangeReport[File]): Set[File] = {
              val files = in.modified -- in.removed
              import sbt._
              inc.Analysis
                .counted("Scala source", "", "s", files.size)
                .foreach { count =>
                  ran = true
                  log.info(s"Executing relayCompile on $count $label...")
                }
              files
            }

            /* I used to count the result, but it was weird and inconsistent */
            FileFunction.cached(cache)(FilesInfo.hash, FilesInfo.exists)(handleUpdate)(scalaFiles)
            if (ran) {
              Def.task[Seq[File]] {
//                val workingDir = (crossTarget in npmUpdate in Compile).value
                val workingDir = file(sys.props("user.dir"))

//                /* Actually run the update */
//                val _      = (npmUpdate in Compile).value
                val logger = streams.value.log
                val sp     = relaySchema.value
                val source = sourceDirectory.value
                runCompiler(workingDir, compilerPath, sp, source, outpath, logger)
                outpath.listFiles()
              }
            } else {
              Def.task[Seq[File]](outpath.listFiles())
            }
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
    import sbt._

    // TODO: this sucks not sure how to get npm scripts to work from java PB.
    val shell = if (System.getProperty("os.name").toLowerCase().contains("win")) {
      List("cmd.exe", "/C")
    } else List("sh")


    val cmd = shell :+ List(compilerPath,
      "--schema",
      schemaPath.getAbsolutePath.quote,
      "--src",
      sourceDirectory.getAbsolutePath.quote,
      "--out",
      outputPath.getAbsolutePath.quote).mkString(" ")
    println(cmd)

    Commands.run(cmd,
                 workingDir,
                 logger)
    ()
  }
}
