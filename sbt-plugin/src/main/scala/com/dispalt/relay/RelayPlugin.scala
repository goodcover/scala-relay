package com.dispalt.relay

import sbt.{AutoPlugin, SettingKey}
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

import sbt.Keys._
import sbt._

import scalajsbundler.util.Commands

object RelayBasePlugin extends AutoPlugin {

  override def requires = ScalaJSPlugin

  override def trigger = noTrigger

  object autoImport {
    val relaySchema: SettingKey[File]           = settingKey[File]("Path to schema file")
    val relayValidateQuery: SettingKey[Boolean] = settingKey[Boolean]("Validate queries in macro")
    val relaySangriaVersion: SettingKey[String] = settingKey[String]("Set the Sangria version")
    val relayScalaJSVersion: SettingKey[String] = settingKey[String]("Set the relay-compiler-language-scalajs version")
    val relayVersion: SettingKey[String]        = settingKey[String]("Set the Relay version")
    val relayDebug: SettingKey[Boolean]         = settingKey[Boolean]("Debug the relay compiler")
    val relayCompile: TaskKey[Seq[File]]        = taskKey[Seq[File]]("Run the relay compiler")
    val relayOutput: SettingKey[File]           = settingKey[File]("Output of the schema stuff")
    val relayCompilerPath: SettingKey[String] =
      settingKey[String]("The location of the `scala-relay-compiler` executable.")
    val relayBaseDirectory: SettingKey[File] = settingKey[File]("The base directory the relay compiler")
    val relayInclude: SettingKey[Seq[File]]  = settingKey[Seq[File]]("extra directories to include")
  }

  val relayFolder = "relay-compiler-out"

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
        /**
          * Set the version of the sangria compiler which helps validate GQL
          */
        relaySangriaVersion := "1.4.2",
        /**
          * Runtime dependency on the macro
          */
        libraryDependencies ++= Seq("com.dispalt.relay"   %%% "relay-macro" % com.dispalt.relay.core.SRCVersion.current,
                                    "org.sangria-graphql" %% "sangria"      % relaySangriaVersion.value % Provided),
        /**
          * Set this if you'd like to see timing and larger stack traces.
          */
        relayDebug := false,
        /**
          * So this should normally default to the base directory, but in some cases if you want to include stuff
          * outside the directory, changing this should be considered.
          */
        relayBaseDirectory := baseDirectory.value,
        /**
          * Get the compiler path from the installed dependencies.
          */
        relayCompilerPath := {
          "node_modules/relay-compiler/lib/RelayCompilerBin.js"
        },
        relayScalaJSVersion := "0.1.5-3",
        relayVersion := "2.0.0",
    ) ++ inConfig(Compile)(perConfigSettings)

  def perConfigSettings: Seq[Setting[_]] =
    Seq(relayCompile := relayCompileTask.value,
        /**
          * Output path of the relay compiler.  Necessary this is an empty directory as it will
          * delete files it thinks went away.
          */
        relayOutput := sourceManaged.value / relayFolder,
        /**
          * Add the NPM Dev Dependency on the scalajs module.
          */
        npmDevDependencies ++= Seq("relay-compiler-language-scalajs" -> relayScalaJSVersion.value,
                                   "relay-compiler"                  -> relayVersion.value),
        /**
          * Include files in the source directory.
          */
        relayInclude := Seq(sourceDirectory.value),
    )

  implicit class QuoteStr(s: String) {
    def quote: String = "\"" + s + "\""
  }

  def relayCompileTask = Def.task[Seq[File]] {
    import Path.relativeTo

    val npmDir        = npmInstallDependencies.value
    val cache         = streams.value.cacheDirectory / "relay-compile"
    val sourceFiles   = unmanagedSourceDirectories.value
    val resourceFiles = resourceDirectories.value
    val outpath       = relayOutput.value
    val compilerPath  = s"node ${(npmDir / relayCompilerPath.value).getPath}"
    val verbose       = relayDebug.value
    val schemaPath    = relaySchema.value
    val source        = relayBaseDirectory.value
    val extras        = relayInclude.value.pair(relativeTo(source)).map(f => f._2 + "/**").toList

    IO.createDirectory(outpath)

    // Filter based on the presence of the annotation. and look for a change
    // in the schema path
    val scalaFiles =
      (sourceFiles ** "*.scala").get
        .filter(IO.read(_).contains("@graphql"))
        .toSet ++ Set(schemaPath) ++ (resourceFiles ** "*.gql").get.toSet
    val label      = Reference.display(thisProjectRef.value)
    val workingDir = file(sys.props("user.dir"))
    val logger     = streams.value.log

    sbt.shim.SbtCompat.FileFunction
      .cached(cache)(FilesInfo.hash, FilesInfo.exists)(
        handleUpdate(label = label,
                     workingDir = workingDir,
                     compilerPath = compilerPath,
                     schemaPath = schemaPath,
                     sourceDirectory = source,
                     outputPath = outpath,
                     logger = logger,
                     verbose = verbose,
                     extras = extras)
      )(scalaFiles)

    outpath.listFiles()
  }

  def runCompiler(workingDir: File,
                  compilerPath: String,
                  schemaPath: File,
                  sourceDirectory: File,
                  outputPath: File,
                  logger: Logger,
                  verbose: Boolean,
                  extras: List[String]): Unit = {

    // TODO: this sucks not sure how to get npm scripts to work from java PB.
    val shell = if (System.getProperty("os.name").toLowerCase().contains("win")) {
      List("cmd.exe", "/C")
    } else List("sh", "-c")

    val verboseList = if (verbose) "--verbose" :: Nil else Nil
    val extrasList  = extras flatMap (e => "--include" :: e.quote :: Nil)

    val cmd = shell :+ (List(compilerPath,
                             "--language",
                             "scalajs",
                             "--watchman",
                             "false",
                             "--schema",
                             schemaPath.getAbsolutePath.quote,
                             "--src",
                             sourceDirectory.getAbsolutePath.quote,
                             "--artifactDirectory",
                             outputPath.getAbsolutePath.quote) ::: verboseList ::: extrasList)
      .mkString(" ")

    println(cmd)
    Commands.run(cmd, workingDir, logger)
  }

  def handleUpdate(label: String,
                   workingDir: File,
                   compilerPath: String,
                   schemaPath: File,
                   sourceDirectory: File,
                   outputPath: File,
                   logger: Logger,
                   verbose: Boolean,
                   extras: List[String])(in: ChangeReport[File], out: ChangeReport[File]): Set[File] = {
    val files = in.modified -- in.removed
    sbt.shim.SbtCompat.Analysis
      .counted("Scala source", "", "s", files.size)
      .foreach { count =>
        logger.info(s"Executing relayCompile on $count $label...")
        runCompiler(workingDir = workingDir,
                    compilerPath = compilerPath,
                    schemaPath = schemaPath,
                    sourceDirectory = sourceDirectory,
                    outputPath = outputPath,
                    logger = logger,
                    verbose = verbose,
                    extras = extras)
        logger.info(s"Finished relayCompile.")
      }
    files
  }
}

object RelayGeneratePlugin extends AutoPlugin {

  override def requires =
    ScalaJSPlugin && RelayBasePlugin

  override def trigger = noTrigger

  import RelayBasePlugin.autoImport._

  override lazy val projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(perConfigSettings)

  def perConfigSettings: Seq[Setting[_]] =
    Seq(
      /**
        * Hook the relay compiler into the compile pipeline.
        */
      sourceGenerators += relayCompile.taskValue
    )

}
