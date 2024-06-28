package com.dispalt.relay

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt.Keys.*
import sbt.{AutoPlugin, Def, SettingKey, *}

import java.io.InputStream

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
    val relayCompilerCommand: TaskKey[String]   = taskKey[String]("The command to execute the `scala-relay-compiler`")
    val relayBaseDirectory: SettingKey[File]    = settingKey[File]("The base directory the relay compiler")
    val relayWorkingDirectory: SettingKey[File] = settingKey[File]("The working directory the relay compiler")
    val relayInclude: SettingKey[Seq[File]]     = settingKey[Seq[File]]("extra directories to include")
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

  import autoImport.*

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
      relayWorkingDirectory := file(sys.props("user.dir")),
      /**
        * Get the compiler path from the installed dependencies.
        */
      relayCompilerCommand := {
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
      relayCompile := relayCompileTask().value,
      /**
        * Run relay-compiler with no caching.
        */
      relayForceCompile := relayCompileTask(force = true).value,
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

  def relayCompileTask(force: Boolean = false) = Def.task[Seq[File]] {
    val outpath         = relayOutput.value
    val compilerCommand = relayCompilerCommand.value
    val verbose         = relayDebug.value
    val schemaPath      = relaySchema.value
    val source          = relayBaseDirectory.value
    val customScalars   = relayCustomScalars.value

    val included         = relayInclude.value
    val extras           = Nil
    val displayOnFailure = relayDisplayOnlyOnFailure.value

    val persisted = relayPersistedPath.value

    val workingDir = relayWorkingDirectory.value
    val logger     = streams.value.log

    // This could be a lot better, since we naturally include the default sourceFiles thing twice.
    val extraWatches      = included
    val cache             = streams.value.cacheDirectory / "relay-compile"
    val sourceDirectories = unmanagedSourceDirectories.value
    val sourceFiles       = unmanagedSources.value.toSet
    val resourceFiles     = resourceDirectories.value

    if (force && persisted.nonEmpty) {
      // @note: Workaround for https://github.com/facebook/relay/issues/2625
      IO.delete(outpath.getAbsoluteFile)
      IO.delete(persisted.get)
    }

    IO.createDirectory(outpath)

    val s = streams.value

    // TODO: This should be its own task.
    // First step is to extract the graphql definitions from the Scala source files and output JavaScript files and
    // Scala.js facades for the final JavaScript that relay-compiler will generate.
    val extractCacheStoreFactory = s.cacheStoreFactory / "graphql-extract"
    // TODO: Make this a setting.
    // TODO: Revisit this. It might have been the relay-compiler output messing things up.
    // sbt annoyingly defaults everything to Compile when we want the setting with a Zero config axis.
    val extractOutputDirectory = sourceManaged.in(ThisScope.copy(config = Zero)).value / "graphql"
    if (force) {
      GraphqlExtractor.clean(extractCacheStoreFactory)
    }
    GraphqlExtractor.extract(extractCacheStoreFactory, sourceFiles, extractOutputDirectory, s.log)

    // The second step we run in two parts. From the graphql files we generate:
    // a) The JavaScript
    // b) The corresponding Scala.js facades
    if (force) {
      RelayCompiler.compile(
        workingDir = workingDir,
        compilerCommand = compilerCommand,
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
    } else {
      // Filter based on the presence of the annotation. and look for a change
      // in the schema path
      val scalaFiles =
        (sourceDirectories ** "*.scala").get.filter { f =>
          val wholeFile = IO.read(f)
          wholeFile.contains("@graphql") || wholeFile.contains("graphqlGen(")
        }.toSet ++ Set(schemaPath) ++ (resourceFiles ** "*.gql").get.toSet ++
          (extraWatches ** "*.gql").get.toSet

      val label = Reference.display(thisProjectRef.value)

      sbt.shim.SbtCompat.FileFunction
        .cached(cache)(FilesInfo.hash, FilesInfo.exists)(
          handleUpdate(
            label = label,
            workingDir = workingDir,
            compilerCommand = compilerCommand,
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
    }

    Seq.empty
  }

  def handleUpdate(
    label: String,
    workingDir: File,
    compilerCommand: String,
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

        RelayCompiler.compile(
          workingDir = workingDir,
          compilerCommand = compilerCommand,
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

  import RelayBasePlugin.autoImport.*

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
