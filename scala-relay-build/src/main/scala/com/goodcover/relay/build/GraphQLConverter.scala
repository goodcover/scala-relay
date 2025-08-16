package com.goodcover.relay.build

import com.goodcover.relay.build.codegen.DocumentConverter
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Converts GraphQL files to Scala.js facades.
 * This is a build-tool agnostic version that can be used by both SBT and Mill.
 */
object GraphQLConverter {
  import FileOps._

  // Increment when the code changes to bust the cache.
  private val Version = 1

  final case class Options(outputDir: File, typeMappings: Map[String, String])

  type Results = Set[File]

  /**
   * Convert GraphQL files to Scala.js facades without caching.
   * This is a simplified version for build tools that handle their own caching.
   */
  def convertSimple(
    sources: Set[File],
    schemaFile: File,
    dependencies: Set[File],
    options: Options,
    logger: BuildLogger
  ): Results = {
    logger.debug("Running GraphqlConverter...")

    if (!schemaFile.exists()) {
      logger.error(s"Schema file does not exist: $schemaFile")
      return Set.empty
    }

    try {
      // Use the DocumentConverter.convertSimple method
      DocumentConverter.convertSimple(sources, schemaFile, dependencies, options, logger)
    } catch {
      case e: Exception =>
        logger.error(s"Failed to convert GraphQL files: ${e.getMessage}")
        Set.empty
    }
  }



  /**
   * Read file content as string
   */
  private def readFile(file: File): String = {
    val source = scala.io.Source.fromFile(file, StandardCharsets.UTF_8.name())
    try {
      source.mkString
    } finally {
      source.close()
    }
  }

  /**
   * Clean up converted files
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
