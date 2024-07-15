package com.goodcover.relay

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt.Keys._
import sbt._
import sbt.io.{ExactFileFilter, ExtensionFilter}

import scala.meta.Dialect

object ScalaRelayBasePlugin extends AutoPlugin {

  override def requires: Plugins = ScalaJSPlugin

  object autoImport {
    val relaySchema: SettingKey[File] = settingKey[File]("Path to schema file")
    val relayTypeScriptPluginVersion: SettingKey[String] =
      settingKey[String]("Set the relay-compiler-language-typescript version")
    val relayTypeScriptVersion: SettingKey[String] = settingKey[String]("Set the typescript version")
    val relayGraphQLVersion: SettingKey[String]    = settingKey[String]("Set the graphql version")
    val relayVersion: SettingKey[String]           = settingKey[String]("Set the Relay version")
    val relayDebug: SettingKey[Boolean]            = settingKey[Boolean]("Set the debug flag for the relay compiler")
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
    val relayExtractDialect: TaskKey[Dialect]   = taskKey[Dialect]("The dialect to use when parsing Scala files")
    val relayConvertTypeMappings: SettingKey[Map[String, String]] =
      settingKey[Map[String, String]]("Mappings from GraphQL types to Scala types")
    val relayGraphQLFiles: TaskKey[Set[File]] = taskKey[Set[File]]("GraphQL files to wrap or convert")
    val relayGraphQLDependencies: TaskKey[Set[File]] = taskKey[Set[File]](
      "GraphQL files that are not themselves wrapped or converted but are required by the other GraphQL files. E.g. The GraphQL files from a project dependency."
    )
    val relayCompilerCommand: TaskKey[String]   = taskKey[String]("The command to execute the `scala-relay-compiler`")
    val relayBaseDirectory: SettingKey[File]    = settingKey[File]("The base directory the relay compiler")
    val relayWorkingDirectory: SettingKey[File] = settingKey[File]("The working directory the relay compiler")
    val relayInclude: SettingKey[Seq[String]] =
      settingKey[Seq[String]]("Globs of directories and files to include, relative to the relayBaseDirectory")
    val relayExclude: SettingKey[Seq[String]] =
      settingKey[Seq[String]]("Globs of directories and files to exclude, relative to the relayBaseDirectory")
    val relayExtensions: SettingKey[Seq[String]] = settingKey[Seq[String]]("File extensions to compile")
    val relayPersistedPath: SettingKey[Option[File]] =
      settingKey[Option[File]]("Where to persist the json file containing the dictionary of all compiled queries.")
    val relayDependencies: SettingKey[Seq[(String, String)]] =
      settingKey[Seq[(String, String)]]("The list of key value pairs that correspond to npm versions")
    val relayDisplayOnlyOnFailure: SettingKey[Boolean] = settingKey("Display output only on failure")

    val relayCompilePersist: TaskKey[Option[File]] = taskKey[Option[File]]("Compile with persisted queries")
    val relayCustomScalars: TaskKey[Map[String, String]] =
      taskKey[Map[String, String]]("translates custom scalars to scala types, **use the full path.**")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
      libraryDependencies ++= Seq("com.goodcover" %%% "scala-relay-core" % com.goodcover.relay.BuildInfo.version),
      relayDebug := false,
      relayDisplayOnlyOnFailure := false,
      relayTypeScript := false,
      relayBaseDirectory := baseDirectory.value,
      relayWorkingDirectory := file(sys.props("user.dir")),
      relayCompilerCommand := "node node_modules/relay-compiler/lib/bin/RelayCompilerBin.js",
      // TODO: Make these all conditional based on one another.
      // Version 14.1.0 uses relay >=10.1.3 and 14.1.1 uses >=12.0.0.
      relayTypeScriptPluginVersion := "14.1.0",
      relayTypeScriptVersion := "^4.2.4",
      relayGraphQLVersion := "^15.0.0",
      relayVersion := "11.0.0"
    ) ++ inConfig(Compile)(perConfigSettings)

  def perConfigSettings: Seq[Setting[_]] =
    Seq(
      relayExtract := relayExtractTask().value,
      relayWrap := relayWrapTask().value,
      relayConvert := relayConvertTask().value,
      relayCompile := relayCompileTask().value,
      relayForceCompile := relayCompileTask(force = true).value,
      relayExtractDirectory := resourceManaged.value / "relay" / "graphql",
      relayWrapDirectory := resourceManaged.value / "relay" / (if (relayTypeScript.value) "ts" else "js"),
      relayConvertDirectory := sourceManaged.value / "relay" / "generated",
      relayCompileDirectory := resourceManaged.value / "__generated__",
      relayExtractDialect := {
        val source3 = scalacOptions.value.contains("-Xsource:3")
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 10))            => scala.meta.dialects.Scala210
          case Some((2, 11))            => scala.meta.dialects.Scala211
          case Some((2, 12)) if source3 => scala.meta.dialects.Scala212Source3
          case Some((2, 12))            => scala.meta.dialects.Scala212
          case Some((2, 13)) if source3 => scala.meta.dialects.Scala213Source3
          case Some((2, 13))            => scala.meta.dialects.Scala213
          case Some((3, 0))             => scala.meta.dialects.Scala30
          case Some((3, 1))             => scala.meta.dialects.Scala31
          case Some((3, 2))             => scala.meta.dialects.Scala32
          case Some((3, 3))             => scala.meta.dialects.Scala33
          case Some((3, 4))             => scala.meta.dialects.Scala33
          case _                        => scala.meta.dialects.Scala3Future
        }
      },
      relayConvertTypeMappings := Map.empty,
      relayGraphQLFiles := graphqlFilesTask.value,
      relayGraphQLDependencies := Set.empty,
      relayDependencies := Seq( //
        "relay-compiler" -> relayVersion.value,
        "graphql"        -> relayGraphQLVersion.value
      ),
      relayDependencies ++= {
        if (relayTypeScript.value)
          Seq(
            "relay-compiler-language-typescript" -> relayTypeScriptPluginVersion.value,
            "typescript"                         -> relayTypeScriptVersion.value
          )
        else Seq.empty
      },
      relayInclude := Seq(relayWrapDirectory.value.relativeTo(relayBaseDirectory.value).get.getPath + "/**"),
      relayExclude := Seq("**/node_modules/**", "**/__mocks__/**", "**/__generated__/**"),
      relayExclude ++= relayCompileDirectory.value.relativeTo(relayBaseDirectory.value).map(_.getPath + "/**"),
      relayExtensions := Seq.empty,
      relayPersistedPath := None,
      relayCompilePersist := relayCompilePersistTask.value,
      relayCustomScalars := Map.empty
    )

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
    val dialect           = relayExtractDialect.value
    val s                 = streams.value
    val cacheStoreFactory = s.cacheStoreFactory / "relay" / "extract"
    if (force) GraphQLExtractor.clean(cacheStoreFactory)
    val extractOptions = GraphQLExtractor.Options(outputDir, dialect)
    GraphQLExtractor.extract(cacheStoreFactory, sourceFiles, extractOptions, s.log)
  }

  // The next step is to wrap all the graphql definitions in graphql macros in the format expected by the relay
  // compiler. The relay compiler is stupid and can't load from graphql files.
  def relayWrapTask(force: Boolean = false): Def.Initialize[Task[Set[File]]] = Def.task {
    val typeScript        = relayTypeScript.value
    val outputDir         = relayWrapDirectory.value
    val graphqlFiles      = relayGraphQLFiles.value
    val s                 = streams.value
    val cacheStoreFactory = s.cacheStoreFactory / "relay" / "wrap"
    if (force) GraphQLWrapper.clean(cacheStoreFactory)
    val extractOptions = GraphQLWrapper.Options(outputDir, typeScript)
    GraphQLWrapper.wrap(cacheStoreFactory, graphqlFiles, extractOptions, s.log)
  }

  def relayConvertTask(force: Boolean = false): Def.Initialize[Task[Set[File]]] = Def.task {
    val schemaFile   = relaySchema.value
    val outputDir    = relayConvertDirectory.value
    val typeMappings = relayConvertTypeMappings.value
    val graphqlFiles = relayGraphQLFiles.value
    val dependencies = relayGraphQLDependencies.value
    val s            = streams.value
    // We also output Scala.js facades for the final JavaScript/TypeScript that the relay-compiler will generate.
    val cacheStoreFactory = s.cacheStoreFactory / "relay" / "convert"
    if (force) GraphQLConverter.clean(cacheStoreFactory)
    val extractOptions = GraphQLConverter.Options(outputDir, typeMappings)
    GraphQLConverter.convert(cacheStoreFactory, graphqlFiles, schemaFile, dependencies, extractOptions, s.log)
  }

  /**
    * GraphQL files, excluding the schema.
    */
  private def graphqlFilesTask = Def.task {
    val schemaFile     = relaySchema.value
    val resourceFiles  = unmanagedResources.value
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
    val excluded         = relayExclude.value
    val extensions       = relayExtensions.value
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

    if (force) RelayCompiler.clean(cacheStoreFactory)

    val compileOptions = RelayCompiler.Options(
      workingDir = workingDir,
      compilerCommand = compilerCommand,
      schemaPath = schemaPath,
      sourceDirectory = source,
      outputPath = outpath,
      verbose = verbose,
      includes = included,
      excludes = excluded,
      extensions = extensions,
      persisted = persisted,
      customScalars = customScalars,
      displayOnFailure = displayOnFailure,
      typeScript = typeScript
    )
    RelayCompiler.compile(cacheStoreFactory, compileOptions, s.log)
  }
}
