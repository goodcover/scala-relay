package com.goodcover.relay.build

import java.io.{File, InputStream}

/**
 * Runs the relay-compiler to generate JavaScript/TypeScript files.
 * This is a build-tool agnostic version that can be used by both SBT and Mill.
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
   * Compile GraphQL files using relay-compiler without caching.
   * This is a simplified version for build tools that handle their own caching.
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
  private def findOutputFiles(outputDir: File): Set[File] = {
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

    val argsList = Seq(
      compilerCommand,
      "--language",
      language,
      "--watchman",
      "false",
      "--schema",
      schemaPath.getAbsolutePath.quote,
      "--src",
      sourceDirectory.getAbsolutePath.quote,
      "--artifactDirectory",
      outputPath.getAbsolutePath.quote
    )

    val verboseList = if (verbose) Seq("--verbose") else Seq.empty
    val includesList = includes.flatMap(include => Seq("--include", include.quote))
    val excludesList = excludes.flatMap(exclude => Seq("--exclude", exclude.quote))
    val extensionsList = extensions.flatMap(extension => Seq("--extensions", extension.quote))
    val persistedList = persisted match {
      case Some(value) => Seq("--persist-output", value.getPath.quote)
      case None => Seq.empty
    }
    val customScalarsArgs = customScalars.map {
      case (scalarType, scalaType) => s"--customScalars.$scalarType=$scalaType"
    }.toSeq

    val cmd = shell :+ (argsList ++ verboseList ++ includesList ++ excludesList ++ extensionsList ++ persistedList ++ customScalarsArgs).mkString(" ")

    var output = Vector.empty[String]

    logger.info("Running relay-compiler...")

    processRunner.run(
      cmd,
      workingDir,
      logger,
      (is: InputStream) => output = scala.io.Source.fromInputStream(is).getLines().toVector
    ) match {
      case Left(error) =>
        output.foreach(logger.error(_))
        throw new RuntimeException(s"Relay compiler failed: $error")
      case Right(_) =>
        if (!displayOnFailure) {
          output.foreach(logger.info(_))
        }
    }
  }

  /**
   * Clean up compiled files
   */
  def clean(outputDir: File): Unit = {
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
}
