package com.goodcover.relay.build

import caliban.parsing.Parser
import caliban.parsing.adt.{Definition, Document}
import caliban.rendering.DocumentRenderer

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.charset.StandardCharsets

/**
 * Wraps GraphQL definitions in JavaScript/TypeScript files with graphql template literals.
 * This is needed because relay-compiler can't load executable definitions from .graphql files.
 */
object GraphQLWrapper {

  case class Options(outputDir: File, typeScript: Boolean)

  type Results = Set[File]

  /**
   * Wrap GraphQL files in JavaScript/TypeScript files without caching.
   * This is a simplified version for build tools that handle their own caching.
   */
  def wrapSimple(sources: Set[File], options: Options, logger: BuildLogger): Results = {
    logger.debug("Running GraphQLWrapper...")

    if (!options.outputDir.exists()) {
      options.outputDir.mkdirs(): Unit
    }

    val results = sources.flatMap { source =>
      wrapFile(source, options, logger)
    }

    results
  }

  /**
   * Wrap a single GraphQL file in a JavaScript/TypeScript file.
   */
  private def wrapFile(file: File, options: Options, logger: BuildLogger): Option[File] = {
    logger.debug(s"Wrapping GraphQL definitions: $file")

    try {
      val extension = if (options.typeScript) "ts" else "js"
      val outputFile = new File(options.outputDir, s"${file.getName.stripSuffix(".graphql")}.$extension")

      val documentText = scala.io.Source.fromFile(file, StandardCharsets.UTF_8.name()).mkString
      val document = Parser.parseQuery(documentText) match {
        case Right(doc) => doc
        case Left(error) =>
          logger.error(s"Failed to parse GraphQL file $file: $error")
          return None
      }

      val writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8))
      try {
        // Write JavaScript/TypeScript header
        if (options.typeScript) {
          writer.write("import { graphql } from 'relay-runtime';\n\n")
        } else {
          writer.write("const { graphql } = require('relay-runtime');\n\n")
        }

        // Wrap each GraphQL definition
        document.definitions.foreach { definition =>
          writeWrapper(writer, definition, document, logger)
        }

        // Write exports
        val exports = document.definitions.map(getDefinitionName).filter(_.nonEmpty)
        if (exports.nonEmpty) {
          if (options.typeScript) {
            writer.write(s"export { ${exports.mkString(", ")} };\n")
          } else {
            writer.write(s"module.exports = { ${exports.mkString(", ")} };\n")
          }
        }

        Some(outputFile)
      } finally {
        writer.close()
      }
    } catch {
      case e: Exception =>
        logger.error(s"Failed to wrap GraphQL file $file: ${e.getMessage}")
        None
    }
  }

  /**
   * Write a single GraphQL definition wrapped in a graphql template literal.
   */
  private def writeWrapper(writer: BufferedWriter, definition: Definition, document: Document, @annotation.unused logger: BuildLogger): Unit = {
    val definitionName = getDefinitionName(definition)
    if (definitionName.nonEmpty) {
      writer.write(s"const $definitionName = graphql`\n")
      val rendered = renderDefinition(definition, document)
      writer.write(escape(trimBlankLines(rendered)))
      writer.write("`;\n\n")
    }
  }

  /**
   * Get the name of a GraphQL definition for use as a JavaScript variable name.
   */
  private def getDefinitionName(definition: Definition): String = {
    definition match {
      case Definition.ExecutableDefinition.OperationDefinition(opType, name, _, _, _) =>
        name.getOrElse(s"Anonymous${opType.toString.capitalize}")
      case Definition.ExecutableDefinition.FragmentDefinition(name, _, _, _) =>
        name
      case _ => ""
    }
  }

  /**
   * Render a GraphQL definition to string format.
   */
  private def renderDefinition(definition: Definition, document: Document): String = {
    trimBlankLines(DocumentRenderer.render(Document(List(definition), document.sourceMapper)))
  }

  /**
   * Escape special characters for JavaScript template literals.
   */
  private def escape(s: String): String = {
    s.replace("\\", "\\\\")
     .replace("`", "\\`")
     .replaceAll("\\$(?=\\{.*?})", "\\\\\\$")
  }

  /**
   * Remove blank lines from the beginning and end of a string.
   */
  private def trimBlankLines(s: String): String = {
    s.split("\n").dropWhile(_.trim.isEmpty).reverse.dropWhile(_.trim.isEmpty).reverse.mkString("\n")
  }
}
