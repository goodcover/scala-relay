package com.dispalt.relay

import java.io.InputStream
import sbt.{AutoPlugin, Def, SettingKey, _}
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Keys._

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
    val relayDisplayOnlyOnFailure: SettingKey[Boolean] = settingKey("Display output only on failure")

    val relayCompilePersist: TaskKey[Option[File]] = taskKey[Option[File]]("Compile with persisted queries")
    val relayCustomScalars: TaskKey[Map[String, String]] =
      taskKey[Map[String, String]]("translates custom scalars to scala types, **use the full path.**")

    // Unset
    val relayNpmDir: TaskKey[File] = taskKey[File]("Set the directory to the parent of node_modules")
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
        "node node_modules/relay-compiler/lib/bin/RelayCompilerBin.js"
      },
      /**
        * The version of the node module
        */
      relayScalaJSVersion := com.dispalt.relay.core.SRCVersion.current,
      /**
        * Set the version of the `relay-compiler` module.
        */
      relayVersion := "11.0.0"
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
      relayOutput := sourceManaged.value / relayFolder / "relay" / "generated",
      /**
        * Add the NPM Dev Dependency on the scalajs module.
        */
      relayDependencies := Seq(
        "relay-compiler-language-scalajs" -> relayScalaJSVersion.value,
        "relay-compiler"                  -> relayVersion.value
      ),
      /**
        * Include files in the source directory.
        */
      relayInclude := Seq(sourceDirectory.value),
      /**
        * Set no use of persistence.
        */
      relayPersistedPath := None,
      /**
        * Display output only on a failure, this works well with persisted queries because they delete all the files
        * before outputting them.
        */
      relayDisplayOnlyOnFailure := false,
      /**
        * Compile conditionally based on persisting a file or not.
        */
      relayCompilePersist := relayCompilePersistTask.value,
      /**
        * Map custom scalar from ScalarType to Scala type
        */
      relayCustomScalars := Map.empty
    )

  implicit class QuoteStr(s: String) {
    def quote: String = "\"" + s + "\""
  }

  def relayCompilePersistTask = Def.taskDyn[Option[File]] {
    if (relayPersistedPath.value.nonEmpty) {
      relayForceCompile.map { _ =>
        relayPersistedPath.value
      }
    } else {
      relayCompile.map { _ =>
        relayPersistedPath.value
      }
    }
  }

  /***
    * REAL SIMILAR TO [[relayForceCompileTask]], update both, factor later.
    * @return
    */
  def relayCompileTask = Def.task[Seq[File]] {
    import Path.relativeTo

    val cache         = streams.value.cacheDirectory / "relay-compile"
    val sourceFiles   = unmanagedSourceDirectories.value
    val resourceFiles = resourceDirectories.value
    val outpath       = relayOutput.value
    val compilerPath  = relayCompilerPath.value
    val verbose       = relayDebug.value
    val schemaPath    = relaySchema.value
    val source        = relayBaseDirectory.value
    // The relay-compiler is really stupid and includes have to be relative to the source directory.
    // We can't use relativeTo from sbt.io as that forces a strict parent child layout.
    val extras           = relayInclude.value.map(f => source.toPath.relativize(f.toPath) + "/**").toList
    val displayOnFailure = relayDisplayOnlyOnFailure.value
    val persisted        = relayPersistedPath.value
    val customScalars    = relayCustomScalars.value

    // This could be a lot better, since we naturally include the default sourceFiles thing twice.
    val extraWatches = relayInclude.value

    IO.createDirectory(outpath)

    // Filter based on the presence of the annotation. and look for a change
    // in the schema path
    val scalaFiles =
      (sourceFiles ** "*.scala").get.filter { f =>
        val wholeFile = IO.read(f)
        wholeFile.contains("@graphql") || wholeFile.contains("graphqlGen(")
      }.toSet ++ Set(schemaPath) ++ (resourceFiles ** "*.gql").get.toSet ++
        (extraWatches ** "*.gql").get.toSet

    val label      = Reference.display(thisProjectRef.value)
    val workingDir = file(sys.props("user.dir"))
    val logger     = streams.value.log

    sbt.shim.SbtCompat.FileFunction
      .cached(cache)(FilesInfo.hash, FilesInfo.exists)(
        handleUpdate(
          label = label,
          workingDir = workingDir,
          compilerPath = compilerPath,
          schemaPath = schemaPath,
          sourceDirectory = source,
          outputPath = outpath,
          logger = logger,
          verbose = verbose,
          extras = extras,
          persisted = persisted,
          customScalars = customScalars,
          displayOnFailure = displayOnFailure
        )
      )(scalaFiles)

    // We can't add persisted file here because it would get wrapped up with the computation
    outpath.listFiles()
  }

  /***
    * REAL SIMILAR TO [[relayCompileTask]], update both, factor later.
    * @return
    */
  def relayForceCompileTask = Def.task[Seq[File]] {
    import Path.relativeTo

    val outpath       = relayOutput.value
    val compilerPath  = relayCompilerPath.value
    val verbose       = relayDebug.value
    val schemaPath    = relaySchema.value
    val source        = relayBaseDirectory.value
    val customScalars = relayCustomScalars.value

    val extras           = relayInclude.value.pair(relativeTo(source)).map(f => f._2 + "/**").toList
    val displayOnFailure = relayDisplayOnlyOnFailure.value

    val persisted = relayPersistedPath.value

    val workingDir = file(sys.props("user.dir"))
    val logger     = streams.value.log

    // @note: Workaround for https://github.com/facebook/relay/issues/2625
    if (persisted.nonEmpty) {
      IO.delete(outpath.getAbsoluteFile)
      IO.delete(persisted.get)
    }

    IO.createDirectory(outpath)

    runCompiler(
      workingDir = workingDir,
      compilerPath = compilerPath,
      schemaPath = schemaPath,
      sourceDirectory = source,
      outputPath = outpath,
      logger = logger,
      verbose = verbose,
      extras = extras,
      persisted = persisted,
      customScalars = customScalars,
      displayOnFailure = displayOnFailure
    )

    val outputFiles = outpath.listFiles()
    logger.info(s"relayForceCompile produced ${outputFiles.size} files.")
    outputFiles
  }

  def runCompiler(
    workingDir: File,
    compilerPath: String,
    schemaPath: File,
    sourceDirectory: File,
    outputPath: File,
    logger: Logger,
    verbose: Boolean,
    extras: List[String],
    persisted: Option[File],
    customScalars: Map[String, String],
    displayOnFailure: Boolean
  ): Unit = {

    // TODO: this sucks not sure how to get npm scripts to work from java PB.
    val shell = if (System.getProperty("os.name").toLowerCase().contains("win")) {
      List("cmd.exe", "/C")
    } else List("sh", "-c")

    val verboseList = if (verbose) "--verbose" :: Nil else Nil
    val extrasList  = extras flatMap (e => "--include" :: e.quote :: Nil)
    val persistedList = persisted match {
      case Some(value) => List("--persist-output", value.getPath.quote)
      case None        => Nil
    }

    val customScalarsArgs = customScalars.map {
      case (scalarType, scalaType) => s"--customScalars.${scalarType}=${scalaType}"
    }.toList

    val cmd = shell :+ (List(
      compilerPath,
      "--language",
      "scalajs",
      "--watchman",
      "false",
      "--schema",
      schemaPath.getAbsolutePath.quote,
      "--src",
      sourceDirectory.getAbsolutePath.quote,
      "--artifactDirectory",
      outputPath.getAbsolutePath.quote
    ) ::: verboseList ::: extrasList ::: persistedList ::: customScalarsArgs)
      .mkString(" ")

    var output = Vector.empty[String]

    Commands.run(
      cmd,
      workingDir,
      logger,
      (is: InputStream) => output = scala.io.Source.fromInputStream(is).getLines.toVector
    ) match {
      case Left(value) =>
        output.foreach(logger.error(_))
        sys.error(s"Relay compiler failed, ${value}")

      case Right(_) =>
        if (!displayOnFailure) {
          output.foreach(logger.info(_))
        }

    }
  }

  def handleUpdate(
    label: String,
    workingDir: File,
    compilerPath: String,
    schemaPath: File,
    sourceDirectory: File,
    outputPath: File,
    logger: Logger,
    verbose: Boolean,
    extras: List[String],
    persisted: Option[File],
    customScalars: Map[String, String],
    displayOnFailure: Boolean
  )(in: ChangeReport[File], out: ChangeReport[File]): Set[File] = {

    val files = in.modified -- in.removed
    sbt.shim.SbtCompat.Analysis
      .counted("Scala source", "", "s", files.size)
      .foreach { count =>
        logger.info(s"Executing relayCompile on $count $label...")

        val lastModified = persisted.map { file =>
          IO.getModifiedTimeOrZero(file)
        }

        runCompiler(
          workingDir = workingDir,
          compilerPath = compilerPath,
          schemaPath = schemaPath,
          sourceDirectory = sourceDirectory,
          outputPath = outputPath,
          logger = logger,
          verbose = verbose,
          extras = extras,
          persisted = persisted,
          customScalars = customScalars,
          displayOnFailure = displayOnFailure
        )

        lastModified.foreach(mtime => IO.setModifiedTimeOrFalse(persisted.get, mtime))
        logger.info(s"Finished relayCompile.")
      }
    files ++ persisted
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
