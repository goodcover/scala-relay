package com.dispalt.relay

import java.io.InputStream

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
    val relayScalaJSVersion: SettingKey[String] = settingKey[String]("Set the relay-compiler-language-scalajs version")
    val relayVersion: SettingKey[String]        = settingKey[String]("Set the Relay version")
    val relayDebug: SettingKey[Boolean]         = settingKey[Boolean]("Set the debug flag for the relay compiler")
    val relayCompile: TaskKey[Seq[File]]        = taskKey[Seq[File]]("Run the relay compiler")
    val relayForceCompile: TaskKey[Seq[File]]   = taskKey[Seq[File]]("Run the relay compiler uncached")
    val relayOutput: SettingKey[File]           = settingKey[File]("Output of the schema stuff")
    val relayCompilerPath: SettingKey[String] =
      settingKey[String]("The location of the `scala-relay-compiler` executable.")
    val relayBaseDirectory: SettingKey[File] = settingKey[File]("The base directory the relay compiler")
    val relayInclude: SettingKey[Seq[File]]  = settingKey[Seq[File]]("extra directories to include")
    val relayPersistedPath: SettingKey[Option[File]] =
      settingKey[Option[File]]("Where to persist the json file containing the dictionary of all compiled queries.")
    val relayDependencies: SettingKey[Seq[(String, String)]] =
      settingKey[Seq[(String, String)]]("The list of key value pairs that correspond to npm versions")
  }

  val relayFolder = "relay-compiler-out"

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
        /**
          * Runtime dependency on the macro
          */
        libraryDependencies ++= Seq("com.dispalt.relay" %%% "relay-macro" % com.dispalt.relay.core.SRCVersion.current),
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
        /**
          * The version of the node module
          */
        relayScalaJSVersion := com.dispalt.relay.core.SRCVersion.current,
        /**
          * Set the version of the `relay-compiler` module.
          */
        relayVersion := "2.0.0",
    ) ++ inConfig(Compile)(perConfigSettings)

  def perConfigSettings: Seq[Setting[_]] =
    Seq(
        /**
          * The big task that performs all the magic.
          */
        relayCompile := relayCompileTask.value,
        /**
          * Run relay-compiler with no caching.
          */
        relayForceCompile := relayForceCompileTask.value,
        /**
          * Output path of the relay compiler.  Necessary this is an empty directory as it will
          * delete files it thinks went away.
          */
        relayOutput := sourceManaged.value / relayFolder,
        /**
          * Add the NPM Dev Dependency on the scalajs module.
          */
        relayDependencies := Seq("relay-compiler-language-scalajs" -> relayScalaJSVersion.value,
                                 "relay-compiler"                  -> relayVersion.value),
        /**
          * Include files in the source directory.
          */
        relayInclude := Seq(sourceDirectory.value),
        /**
          * Set no use of persistence.
          */
        relayPersistedPath := None,
    )

  implicit class QuoteStr(s: String) {
    def quote: String = "\"" + s + "\""
  }

  /***
    * REAL SIMILAR TO [[relayForceCompileTask]], update both, factor later.
    * @return
    */
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
    val persisted     = relayPersistedPath.value
    val extras        = relayInclude.value.pair(relativeTo(source)).map(f => f._2 + "/**").toList

    // This could be a lot better, since we naturally include the default sourceFiles thing twice.
    val extraWatches = relayInclude.value

    IO.createDirectory(outpath)

    // Filter based on the presence of the annotation. and look for a change
    // in the schema path
    val scalaFiles =
      (sourceFiles ** "*.scala").get
        .filter(IO.read(_).contains("@graphql"))
        .toSet ++ Set(schemaPath) ++ (resourceFiles ** "*.gql").get.toSet ++
        (extraWatches ** "*.gql").get.toSet

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
                     extras = extras,
                     persisted = persisted)
      )(scalaFiles)

    outpath.listFiles()
  }

  /***
    * REAL SIMILAR TO [[relayCompileTask]], update both, factor later.
    * @return
    */
  def relayForceCompileTask = Def.task[Seq[File]] {
    import Path.relativeTo

    val npmDir       = npmInstallDependencies.value
    val outpath      = relayOutput.value
    val compilerPath = s"node ${(npmDir / relayCompilerPath.value).getPath}"
    val verbose      = relayDebug.value
    val schemaPath   = relaySchema.value
    val source       = relayBaseDirectory.value
    val persisted    = relayPersistedPath.value
    val extras       = relayInclude.value.pair(relativeTo(source)).map(f => f._2 + "/**").toList

    val workingDir = file(sys.props("user.dir"))
    val logger     = streams.value.log

    IO.createDirectory(outpath)

    runCompiler(workingDir = workingDir,
                compilerPath = compilerPath,
                schemaPath = schemaPath,
                sourceDirectory = source,
                outputPath = outpath,
                logger = logger,
                verbose = verbose,
                extras = extras,
                persisted = persisted)

    outpath.listFiles()
  }

  def runCompiler(workingDir: File,
                  compilerPath: String,
                  schemaPath: File,
                  sourceDirectory: File,
                  outputPath: File,
                  logger: Logger,
                  verbose: Boolean,
                  extras: List[String],
                  persisted: Option[File]): Unit = {

    // TODO: this sucks not sure how to get npm scripts to work from java PB.
    val shell = if (System.getProperty("os.name").toLowerCase().contains("win")) {
      List("cmd.exe", "/C")
    } else List("sh", "-c")

    val verboseList = if (verbose) "--verbose" :: Nil else Nil
    val extrasList  = extras flatMap (e => "--include" :: e.quote :: Nil)
    val persistedList = persisted match {
      case Some(value) => List("--persist-output", value.getPath)
      case None        => Nil
    }

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
                             outputPath.getAbsolutePath.quote) ::: verboseList ::: extrasList ::: persistedList)
      .mkString(" ")

//    println(cmd)
    val toInfoLog = (is: InputStream) => scala.io.Source.fromInputStream(is).getLines.foreach(msg => logger.info(msg))

    Commands.run(cmd, workingDir, logger, toInfoLog)
  }

  def handleUpdate(label: String,
                   workingDir: File,
                   compilerPath: String,
                   schemaPath: File,
                   sourceDirectory: File,
                   outputPath: File,
                   logger: Logger,
                   verbose: Boolean,
                   extras: List[String],
                   persisted: Option[File])(in: ChangeReport[File], out: ChangeReport[File]): Set[File] = {
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
                    extras = extras,
                    persisted = persisted)
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
