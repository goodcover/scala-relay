package com.goodcover.relay.mill

import com.goodcover.relay.build.*
import com.goodcover.relay.build.codegen.DocumentConverter
import mill.*
import mill.api._
import mill.scalalib.ScalaModule

import java.io.File
import scala.meta.dialects

/**
  * Mill module trait that provides Relay GraphQL compilation tasks.
  * Mix this trait into your ScalaModule to add Relay support.
  */
trait RelayModule extends ScalaModule {

  /**
    * The GraphQL schema file
    */
  def relaySchemaFile: Task[PathRef] = Task {
    PathRef(moduleDir / "schema.graphql")
  }

  /**
    * Directory where extracted GraphQL files will be placed
    */
  def relayExtractDir: Task[PathRef] = Task {
    PathRef(Task.dest / "extracted")
  }

  /**
    * Directory where converted Scala.js facades will be placed
    */
  def relayConvertDir: Task[PathRef] = Task {
    PathRef(Task.dest / "converted")
  }

  /**
    * Directory where relay-compiler output will be placed
    */
  def relayCompileDir: Task[PathRef] = Task {
    PathRef(Task.dest / "compiled")
  }

  /**
    * Command to run the relay compiler
    */
  def relayCompilerCommand: Task[String] = Task {
    "relay-compiler"
  }

  /**
    * Type mappings for GraphQL to Scala conversion
    */
  def relayTypeMappings: Task[Map[String, String]] = Task {
    Map.empty[String, String]
  }

  /**
    * Whether to enable verbose output from relay-compiler
    */
  def relayVerbose: Task[Boolean] = Task {
    false
  }

  /**
    * Include patterns for relay-compiler
    */
  def relayIncludes: Task[Seq[String]] = Task {
    Seq("**/*.graphql")
  }

  /**
    * Exclude patterns for relay-compiler
    */
  def relayExcludes: Task[Seq[String]] = Task {
    Seq.empty[String]
  }

  /**
    * File extensions for relay-compiler
    */
  def relayExtensions: Task[Seq[String]] = Task {
    Seq("js", "ts")
  }

  /**
    * Whether to generate TypeScript instead of JavaScript
    */
  def relayTypeScript: Task[Boolean] = Task {
    false
  }

  /**
    * Extract GraphQL definitions from Scala source files
    */
  def relayExtract: Task[PathRef] = Task {
    implicit val ctx: TaskCtx = Task.ctx()
    val logger                = MillBuildLogger(ctx.log)

    val sourceFiles = allSources().flatMap(_.path.toIO.listFiles()).filter(_.getName.endsWith(".scala")).toSet
    val outputDir   = relayExtractDir().path.toIO

    val options = GraphQLExtractor.Options(outputDir, dialects.Scala213)
    val results = GraphQLExtractor.extractSimple(sourceFiles, options, logger)

    PathRef(outputDir)
  }

  /**
    * Convert GraphQL files to Scala.js facades
    */
  def relayConvert: Task[PathRef] = Task {
    implicit val ctx: TaskCtx = Task.ctx()
    val logger                = MillBuildLogger(ctx.log)

    val extractedDir = relayExtract().path.toIO
    val schemaFile   = relaySchemaFile().path.toIO
    val outputDir    = relayConvertDir().path

    if (!schemaFile.exists()) {
      logger.error(s"Schema file does not exist: $schemaFile")
      return PathRef(outputDir)
    }

    val graphqlFiles = if (extractedDir.exists()) {
      extractedDir.listFiles().filter(_.getName.endsWith(".graphql")).toSet
    } else {
      Set.empty[File]
    }

    val options = GraphQLConverter.Options(outputDir, relayTypeMappings())
    val results = GraphQLConverter.convertSimple(
      graphqlFiles,
      schemaFile,
      Set.empty, // dependencies
      options,
      logger
    )

    PathRef(outputDir)
  }

  /**
    * Run relay-compiler to generate JavaScript/TypeScript files
    */
  def relayCompile: Task[PathRef] = Task {
    implicit val ctx: TaskCtx = Task.ctx()
    val logger                = MillBuildLogger(ctx.log)
    val processRunner         = MillProcessRunner()

    val extractedDir = relayExtract().path.toIO
    val schemaFile   = relaySchemaFile().path.toIO
    val outputDir    = relayCompileDir().path.toIO

    if (!schemaFile.exists()) {
      logger.error(s"Schema file does not exist: $schemaFile")
      return PathRef(outputDir)
    }

    if (!extractedDir.exists() || extractedDir.listFiles().isEmpty) {
      logger.warn("No GraphQL files found to compile")
      return PathRef(outputDir)
    }

    val options = RelayCompiler.Options(
      workingDir = millSourcePath.toIO,
      compilerCommand = relayCompilerCommand(),
      schemaPath = schemaFile,
      sourceDirectory = extractedDir,
      outputPath = outputDir,
      verbose = relayVerbose(),
      includes = relayIncludes(),
      excludes = relayExcludes(),
      extensions = relayExtensions(),
      persisted = None,
      customScalars = Map.empty,
      displayOnFailure = true,
      typeScript = relayTypeScript()
    )

    val results = RelayCompiler.compileSimple(options, logger, processRunner)

    PathRef(outputDir)
  }

  /**
    * Run all Relay tasks in sequence
    */
  def relayAll: Task[PathRef] = Task {
    relayExtract()
    relayConvert()
    relayCompile()
  }

  /**
    * Clean all Relay generated files
    */
  def relayClean: Task[Unit] = Task {
    val extractDir = relayExtractDir().path.toIO
    val convertDir = relayConvertDir().path.toIO
    val compileDir = relayCompileDir().path.toIO

    GraphQLExtractor.clean(extractDir)
    GraphQLConverter.clean(convertDir)
    RelayCompiler.clean(compileDir)
  }
}

object RelayModule {

  /**
    * Helper to create a ScalaModule with Relay support
    */
  trait Default extends RelayModule {
    // Provides sensible defaults for most use cases
  }
}
