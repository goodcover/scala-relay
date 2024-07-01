package com.dispalt.relay

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt.Keys.*
import sbt.io.{ExactFileFilter, ExtensionFilter}
import sbt.{AutoPlugin, Def, SettingKey, *}

object RelayBasePlugin extends AutoPlugin {

  override def requires: Plugins = ScalaJSPlugin

  override def trigger = noTrigger

  object autoImport {
    val relaySchema: SettingKey[File] = settingKey[File]("Path to schema file")
    // TODO: Remove.
    val relayScalaJSVersion: SettingKey[String] = settingKey[String]("Set the relay-compiler-language-scalajs version")
    val relayVersion: SettingKey[String]        = settingKey[String]("Set the Relay version")
    val relayDebug: SettingKey[Boolean]         = settingKey[Boolean]("Set the debug flag for the relay compiler")
    val relayTypeScript: SettingKey[Boolean] = settingKey[Boolean](
      "If true, sets the language to TypeScript. If false, sets the language to JavaScript. Defaults to false. This is" +
        "only really useful as a temporary step to compare against the generated Scala.js facades."
    )
    val relayExtract: TaskKey[Set[File]] =
      taskKey[Set[File]]("Extracts the graphql definitions from the Scala sources")
    val relayWrap: TaskKey[Set[File]] =
      taskKey[Set[File]](
        "Wraps all the graphql definitions in graphql macros in the format expected by the relay compiler"
      )
    val relayConvert: TaskKey[Set[File]] =
      taskKey[Set[File]](
        "Converts the graphql definitions to Scala.js facades that match the output from the relay compiler"
      )
    val relayCompile: TaskKey[Set[File]]      = taskKey[Set[File]]("Run the relay compiler")
    val relayForceCompile: TaskKey[Set[File]] = taskKey[Set[File]]("Run the relay compiler uncached")
    val relayExtractDirectory: SettingKey[File] =
      settingKey[File]("Output directory for the extracted resources containing the graphql definitions")
    val relayWrapDirectory: SettingKey[File] = settingKey[File](
      "Output directory for the generated JavaScript/TypeScript sources containing the graphql macros for the relay-compiler"
    )
    val relayConvertDirectory: SettingKey[File] =
      settingKey[File]("Output directory for the generated Scala.js facades that match the generate")
    val relayCompileDirectory: SettingKey[File] = settingKey[File]("Output of the schema stuff")
    val relayCompilerCommand: TaskKey[String]   = taskKey[String]("The command to execute the `scala-relay-compiler`")
    val relayBaseDirectory: SettingKey[File]    = settingKey[File]("The base directory the relay compiler")
    val relayWorkingDirectory: SettingKey[File] = settingKey[File]("The working directory the relay compiler")
    val relayInclude: SettingKey[Seq[String]] =
      settingKey[Seq[String]]("Globs of files to include, relative to the relayBaseDirectory")
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
      // TODO: Change this back to false.
      relayTypeScript := true,
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
      relayExtract := relayExtractTask().value,
      relayWrap := relayWrapTask().value,
      relayConvert := relayConvertTask().value,
      /**
        * The big task that performs all the magic.
        */
      relayCompile := relayCompileTask().value,
      /**
        * Run relay-compiler with no caching.
        */
      relayForceCompile := relayCompileTask(force = true).value,
      relayExtractDirectory := sourceManagedRoot.value / "relay" / "graphql",
      relayWrapDirectory := sourceManagedRoot.value / "relay" / (if (relayTypeScript.value) "ts" else "js"),
      relayConvertDirectory := sourceManaged.value / "relay" / "scala",
      /**
        * Output path of the relay compiler. Necessary this is an empty directory as it will
        * assume that all files contained within it are artifacts from relay.
        */
      relayCompileDirectory := sourceManagedRoot.value / "relay" / "generated",
      /**
        * Add the NPM Dev Dependency on the scalajs module.
        */
      relayDependencies := Seq(
        // TODO: Remove.
        "relay-compiler-language-scalajs" -> relayScalaJSVersion.value,
        "relay-compiler"                  -> relayVersion.value
      ),
      /**
        * Include files in the base directory.
        */
      relayInclude :=
        (relayExtractDirectory.value +: resourceDirectories.value)
          .map(_.relativeTo(relayBaseDirectory.value).get.getPath + "/**"),
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

  private def sourceManagedRoot = Def.setting {
    // sbt annoyingly defaults everything to Compile when we want the setting with a Zero config axis.
    sourceManaged.in(ThisScope.copy(config = Zero)).value
  }

  private def relayCompilePersistTask = Def.taskDyn[Option[File]] {
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

  // First step is to extract the graphql definitions from the Scala source files and output them as graphql files.
  def relayExtractTask(force: Boolean = false): Def.Initialize[Task[Set[File]]] = Def.task {
    val sourceFiles       = unmanagedSources.value.toSet
    val outputDir         = relayExtractDirectory.value
    val s                 = streams.value
    val cacheStoreFactory = s.cacheStoreFactory / "relay" / "extract"
    if (force) {
      GraphQLExtractor.clean(cacheStoreFactory)
    }
    val extractOptions = GraphQLExtractor.Options(outputDir)
    GraphQLExtractor.extract(cacheStoreFactory, sourceFiles, extractOptions, s.log)
  }

  // The next step is to wrap all the graphql definitions in graphql macros in the format expected by the relay
  // compiler. The relay compiler is stupid and can't load from graphql files.
  def relayWrapTask(force: Boolean = false): Def.Initialize[Task[Set[File]]] = Def.task {
    val typeScript        = relayTypeScript.value
    val outputDir         = relayWrapDirectory.value
    val graphqlFiles      = graphqlFilesTask.value
    val s                 = streams.value
    val cacheStoreFactory = s.cacheStoreFactory / "relay" / "wrap"
    if (force) {
      GraphQLWrapper.clean(cacheStoreFactory)
    }
    val extractOptions = GraphQLWrapper.Options(outputDir, typeScript)
    GraphQLWrapper.wrap(cacheStoreFactory, graphqlFiles, extractOptions, s.log)
  }

  def relayConvertTask(force: Boolean = false): Def.Initialize[Task[Set[File]]] = Def.task {
    val schemaFile   = relaySchema.value
    val outputDir    = relayConvertDirectory.value
    val graphqlFiles = graphqlFilesTask.value
    val s            = streams.value
    // We also output Scala.js facades for the final JavaScript/TypeScript that the relay-compiler will generate.
    val cacheStoreFactory = s.cacheStoreFactory / "relay" / "convert"
    if (force) {
      GraphQLConverter.clean(cacheStoreFactory)
    }
    val extractOptions = GraphQLConverter.Options(outputDir)
    GraphQLConverter.convert(cacheStoreFactory, graphqlFiles, schemaFile, extractOptions, s.log)
  }

  /**
    * GraphQL files, excluding the schema.
    */
  private def graphqlFilesTask = Def.task {
    val schemaFile    = relaySchema.value
    val resourceFiles = resources.value
    // These should already be in resources but that could be changed so include them explicitly too.
    val extractedFiles = relayExtract.value
    val filter         = new ExtensionFilter("gql", "graphql") -- new ExactFileFilter(schemaFile)
    resourceFiles.filter(filter.accept).toSet ++ extractedFiles.filter(filter.accept)
  }

  def relayCompileTask(force: Boolean = false): Def.Initialize[Task[Set[File]]] = Def.task {
    val _ = relayWrap.value

    val outpath          = relayCompileDirectory.value
    val compilerCommand  = relayCompilerCommand.value
    val verbose          = relayDebug.value
    val schemaPath       = relaySchema.value
    val source           = relayBaseDirectory.value
    val customScalars    = relayCustomScalars.value
    val included         = relayInclude.value
    val displayOnFailure = relayDisplayOnlyOnFailure.value
    val persisted        = relayPersistedPath.value
    val workingDir       = relayWorkingDirectory.value
    val typeScript       = relayTypeScript.value
    val s                = streams.value

    // TODO: Do we still need this?
    if (force && persisted.nonEmpty) {
      // @note: Workaround for https://github.com/facebook/relay/issues/2625
      IO.delete(outpath.getAbsoluteFile)
      IO.delete(persisted.get)
    }

    IO.createDirectory(outpath)

    // We give the JavaScript/TypeScript files from above that contain the graphql "macros" to the relay-compiler and
    // get it to do its usual thing, completely unaware of Scala.js since it no longer supports language plugins.
    val cacheStoreFactory = s.cacheStoreFactory / "relay" / "compile"

    if (force) {
      RelayCompiler.clean(cacheStoreFactory)
    }

    val compileOptions = RelayCompiler.Options(
      workingDir = workingDir,
      compilerCommand = compilerCommand,
      schemaPath = schemaPath,
      sourceDirectory = source,
      outputPath = outpath,
      verbose = verbose,
      includes = included,
      persisted = persisted,
      customScalars = customScalars,
      displayOnFailure = displayOnFailure,
      typeScript = typeScript
    )
    RelayCompiler.compile(cacheStoreFactory, compileOptions, s.log)
  }
}
