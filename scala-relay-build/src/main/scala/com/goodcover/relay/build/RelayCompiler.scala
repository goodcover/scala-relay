package com.goodcover.relay.build

import java.io.{ File, InputStream }

/**
 * Runs the relay-compiler to generate JavaScript/TypeScript files. This is a
 * build-tool agnostic version that can be used by both SBT and Mill.
 */
object RelayCompiler {
  import FileOps._

  // Increment when the code changes to bust the cache.
  private val Version = 1

  private implicit class QuoteStr(val s: String) extends AnyVal {
    def quote: String = {
      require(!s.contains('\''))
      "'" + s + "'"
    }
  }

  final case class Options(
    workingDir: File,
    compilerCommand: String,
    schemaPath: File,
    sourceDirectory: File,
    outputPath: File,
    verbose: Boolean,
    includes: Seq[String],
    excludes: Seq[String],
    extensions: Seq[String],
    persisted: Option[File],
    customScalars: Map[String, String],
    displayOnFailure: Boolean,
    typeScript: Boolean
  ) {
    def language: String = if (typeScript) "typescript" else "javascript"
  }

  type Results = Set[File]

  /**
   * Compile GraphQL files using relay-compiler without caching. This is a
   * simplified version for build tools that handle their own caching.
   */
  def compileSimple(
    options: Options,
    logger: BuildLogger,
    processRunner: ProcessRunner = new DefaultProcessRunner()
  ): Results = {
    logger.debug("Running RelayCompiler...")

    // Ensure output directory exists
    options.outputPath.mkdirs()

    // Find existing output files before compilation
    val existingFiles = findOutputFiles(options.outputPath)

    // Run the relay compiler
    runRelayCompiler(options, logger, processRunner)

    // Find new output files after compilation
    val newFiles = findOutputFiles(options.outputPath)

    newFiles
  }

  /**
   * Find all output files in the output directory
   */
  private def findOutputFiles(outputDir: File): Set[File] =
    if (!outputDir.exists()) {
      Set.empty
    } else {
      def findFiles(dir: File): Set[File] = {
        val files = dir.listFiles()
        if (files == null) Set.empty
        else {
          files.toSet.flatMap { (file: File) =>
            if (file.isDirectory) {
              findFiles(file)
            } else {
              Set(file)
            }
          }
        }
      }
      findFiles(outputDir)
    }

  /**
   * Run the relay compiler command
   */
  private def runRelayCompiler(
    options: Options,
    logger: BuildLogger,
    processRunner: ProcessRunner
  ): Unit = {
    val Options(
      workingDir,
      compilerCommand,
      schemaPath,
      sourceDirectory,
      outputPath,
      verbose,
      includes,
      excludes,
      extensions,
      persisted,
      customScalars,
      displayOnFailure,
      typeScript
    ) = options

    val language = options.language

    // Build the command
    val shell = if (System.getProperty("os.name").toLowerCase().contains("win")) {
      Seq("cmd.exe", "/C")
    } else {
      Seq("sh", "-c")
    }

    // Check if relay.config.js exists in working directory
    val relayConfigFile = new File(workingDir, "relay.config.js")
    val packageJsonFile = new File(workingDir, "package.json")

    val useConfigFileOnly = relayConfigFile.exists() || packageJsonFile.exists()

    val argsList = if (useConfigFileOnly) {
      // Modern relay-compiler v17.0.0+ with config file - no CLI arguments needed
      logger.debug("Using relay.config.js or package.json configuration")
      Seq(compilerCommand)
    } else {
      // Fallback to CLI arguments for older versions or when no config file exists
      logger.debug("No config file found, using CLI arguments")
      Seq(
        compilerCommand,
        "--schema",
        schemaPath.getAbsolutePath.quote,
        "--src",
        sourceDirectory.getAbsolutePath.quote,
        "--artifactDirectory",
        outputPath.getAbsolutePath.quote
      )
    }

    // Modern verbosity control (only if not using config file)
    val verboseList = if (useConfigFileOnly) {
      Seq.empty // Verbosity configured in config file
    } else if (verbose) {
      Seq("--output", "verbose")
    } else {
      Seq("--output", "quiet-with-errors")
    }

    // Note: Modern relay-compiler uses config files for includes, excludes, extensions, etc.
    // These command-line options are no longer supported in v17.0.0+
    val includesList      = Seq.empty[String] // No longer supported via CLI
    val excludesList      = Seq.empty[String] // No longer supported via CLI
    val extensionsList    = Seq.empty[String] // No longer supported via CLI
    val persistedList     = if (useConfigFileOnly) {
      Seq.empty // Configured in config file
    } else {
      persisted match {
        case Some(_) => Seq("--repersist") // Modern equivalent
        case None    => Seq.empty
      }
    }
    // Custom scalars are now configured via relay.config.js, not CLI
    val customScalarsArgs = Seq.empty[String]

    val cmd =
      shell :+ (argsList ++ verboseList ++ includesList ++ excludesList ++ extensionsList ++ persistedList ++ customScalarsArgs)
        .mkString(" ")

    var output = Vector.empty[String]

    logger.info("Running relay-compiler...")
    logger.debug(s"Command: ${cmd.mkString(" ")}")
    logger.debug(s"Working directory: ${workingDir.getAbsolutePath}")
    logger.debug(s"Schema path: ${schemaPath.getAbsolutePath}")
    logger.debug(s"Source directory: ${sourceDirectory.getAbsolutePath}")
    logger.debug(s"Output path: ${outputPath.getAbsolutePath}")

    processRunner.run(
      cmd,
      workingDir,
      logger,
      (is: InputStream) => output = scala.io.Source.fromInputStream(is).getLines().toVector
    ) match {
      case Left(error) =>
        output.foreach(logger.error(_))
        throw new RuntimeException(s"Relay compiler failed: $error")
      case Right(_)    =>
        if (!displayOnFailure) {
          output.foreach(logger.info(_))
        }
    }
  }

  /**
   * Clean up compiled files
   */
  def clean(outputDir: File): Unit =
    if (outputDir.exists()) {
      def deleteRecursively(file: File): Unit = {
        if (file.isDirectory) {
          file.listFiles().foreach(deleteRecursively)
        }
        file.delete()
      }
      deleteRecursively(outputDir)
    }
}
