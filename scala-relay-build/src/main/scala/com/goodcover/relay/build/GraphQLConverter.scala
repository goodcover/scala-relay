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

  type Conversions = Map[File, Set[File]]

  // TODO: Add parallelism.
  def convertFiles(files: Iterable[File], schema: GraphQLSchema, options: Options, logger: BuildLogger): Conversions = {
    logger.debug(s"Converting schema: ${schema.file}")
    val converter          = new DocumentConverter(options.outputDir, schema, options.typeMappings, Set.empty[File])
    val outputs            = converter.convertSchema()
    val initialConversions = Map(schema.file -> outputs)
    val (conversions, _) = files.foldLeft((initialConversions, outputs)) {
      case ((conversions, outputs), file) =>
        logger.debug(s"Converting file: $file")
        // This is kind of silly since only the outputs change but oh well, it saves passing them around everywhere.
        val converter       = new DocumentConverter(options.outputDir, schema, options.typeMappings, outputs)
        val newOutputs      = converter.convert(file)
        val nextConversions = conversions + (file -> newOutputs)
        val nextOutputs     = outputs ++ newOutputs
        (nextConversions, nextOutputs)
    }
    conversions
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
