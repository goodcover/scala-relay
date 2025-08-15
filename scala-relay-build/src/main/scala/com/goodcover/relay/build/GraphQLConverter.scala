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
      // Read schema and create GraphQLSchema instance
      val schema = GraphQLSchema(schemaFile, dependencies)

      // Convert each GraphQL file
      val outputs = sources.flatMap { file =>
        convertFile(file, schema, options, logger)
      }

      // Also convert schema types
      val schemaOutputs = convertSchema(schema, options, logger)

      outputs ++ schemaOutputs
    } catch {
      case e: Exception =>
        logger.error(s"Failed to convert GraphQL files: ${e.getMessage}")
        Set.empty
    }
  }

  /**
   * Convert a single GraphQL file to Scala.js facades.
   */
  private def convertFile(
    file: File,
    schema: GraphQLSchema,
    options: Options,
    logger: BuildLogger
  ): Set[File] = {
    logger.debug(s"Converting GraphQL file: $file")

    try {
      val documentText = readFile(file)
      val converter = new DocumentConverter(options.outputDir, schema, options.typeMappings, Set.empty)
      converter.convert(documentText)
    } catch {
      case e: Exception =>
        logger.error(s"Failed to convert $file: ${e.getMessage}")
        Set.empty
    }
  }

  /**
   * Convert schema types to Scala.js facades.
   */
  private def convertSchema(
    schema: GraphQLSchema,
    options: Options,
    logger: BuildLogger
  ): Set[File] = {
    logger.debug("Converting schema types")

    try {
      val converter = new DocumentConverter(options.outputDir, schema, options.typeMappings, Set.empty)
      converter.convertSchema()
    } catch {
      case e: Exception =>
        logger.error(s"Failed to convert schema: ${e.getMessage}")
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
