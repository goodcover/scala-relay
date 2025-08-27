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

  private def dialect = Task.Anon {
    scalaVersion() match {
      case v if v.startsWith("2.12") => dialects.Scala210
      case v if v.startsWith("2.13") => dialects.Scala213
      case v if v.startsWith("3.0")  => dialects.Scala30
      case v if v.startsWith("3.1")  => dialects.Scala31
      case v if v.startsWith("3.2")  => dialects.Scala32
      case v if v.startsWith("3.3")  => dialects.Scala33
      case v if v.startsWith("3.4")  => dialects.Scala34
      case v if v.startsWith("3.5")  => dialects.Scala35
      case v if v.startsWith("3.6")  => dialects.Scala36
      case v if v.startsWith("3.7")  => dialects.Scala37
      case _                         => dialects.Scala3Future
    }
  }

  /**
    * Extract GraphQL definitions from Scala source files
    */
  def relayExtract: Task[PathRef] = Task {
    val logger = MillBuildLogger(Task.log)

    val sourceFiles = allSources().flatMap(_.path.toIO.listFiles()).filter(_.getName.endsWith(".scala")).toSet
    val outputDir   = relayExtractDir().path.toIO

    val d = dialect()

    val options = GraphQLExtractor.Options(outputDir, d)

    val sourcePairs = GraphQLExtractor.sourceOutputs(sourceFiles.toSeq, options)

    val results = GraphQLExtractor.extractFiles(sourcePairs, options.dialect, logger)

    PathRef(os.Path(outputDir))
  }

  /**
    * Directory for wrapped JavaScript/TypeScript files
    */
  def relayWrapDir: Task[PathRef] = Task {
    PathRef(Task.dest / "wrap")
  }

  /**
    * Wrap GraphQL definitions in JavaScript/TypeScript files for relay-compiler
    */
  def relayWrap: Task[PathRef] = Task {
    val logger     = MillBuildLogger(Task.log)
    val extractDir = relayExtract().path.toIO
    val outputDir  = relayWrapDir().path.toIO
    val typeScript = relayTypeScript()

    // Find all .graphql files from the extract step
    val graphqlFiles = if (extractDir.exists()) {
      extractDir.listFiles().filter(_.getName.endsWith(".graphql")).toSet
    } else {
      Set.empty[File]
    }

    val options = GraphQLWrapper.Options(outputDir, typeScript)

    val files   = GraphQLWrapper.resourceOutputs(graphqlFiles.toSeq, options)
    val results = GraphQLWrapper.wrapFiles(files, logger)

    PathRef(os.Path(outputDir))
  }

  /**
    * Convert GraphQL files to Scala.js facades
    */
  def relayConvert: Task[PathRef] = Task {
    val logger = MillBuildLogger(Task.log)

    val extractedDir: File = relayExtract().path.toIO
    val schemaFile: File   = relaySchemaFile().path.toIO
    val outputDir: File    = relayConvertDir().path.toIO

    if (!schemaFile.exists()) {
      logger.error(s"Schema file does not exist: $schemaFile")
      PathRef(os.Path(outputDir))
    } else {
      val graphqlFiles = if (extractedDir.exists()) {
        extractedDir.listFiles().filter(_.getName.endsWith(".graphql")).toSet
      } else {
        Set.empty[File]
      }

      val schema  = GraphQLSchema(schemaFile, Set.empty)
      val options = GraphQLConverter.Options(outputDir, relayTypeMappings())
      val results = GraphQLConverter.convertFiles( //
        graphqlFiles,
        schema,
        options,
        logger
      )

      PathRef(os.Path(outputDir))
    }
  }

  /**
    * Run relay-compiler to generate JavaScript/TypeScript files
    */
  def relayCompile: Task[PathRef] = Task {
    val logger        = MillBuildLogger(Task.log)
    val processRunner = MillProcessRunner()

    val wrappedDir = relayWrap().path.toIO // Use wrapped files instead of extracted
    val schemaFile = relaySchemaFile().path.toIO
    val outputDir  = relayCompileDir().path.toIO

    if (!schemaFile.exists()) {
      logger.error(s"Schema file does not exist: $schemaFile")
      PathRef(os.Path(outputDir))
    } else if (!wrappedDir.exists() || wrappedDir.listFiles().isEmpty) {
      logger.warn("No wrapped GraphQL files found to compile")
      PathRef(os.Path(outputDir))
    } else {
      val options = RelayCompiler.Options(
        workingDir = moduleDir.toIO,
        compilerCommand = relayCompilerCommand(),
        schemaPath = schemaFile,
        sourceDirectory = wrappedDir, // Point to wrapped files
        outputPath = outputDir,
        verbose = relayVerbose(),
        includes = relayIncludes(),
        excludes = relayExcludes(),
        extensions = relayExtensions(),
        persisted = None,
        customScalars = Map.empty[String, String],
        displayOnFailure = true,
        typeScript = relayTypeScript()
      )

      val results = RelayCompiler.compileSimple(options, logger, processRunner)

      PathRef(os.Path(outputDir))
    }
  }

  /**
    * Run all Relay tasks in sequence
    */
  def relayAll: Task[PathRef] = Task {
    relayExtract()
    relayWrap()
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
