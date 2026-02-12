package com.goodcover.relay.build

import FileOps._
import caliban.parsing.Parser
import caliban.parsing.adt.{ Definition, Document }
import caliban.rendering.DocumentRenderer

import java.io.{ BufferedWriter, File, FileWriter }
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.collection.Seq

/**
 * Wraps GraphQL definitions in JavaScript/TypeScript files with graphql
 * template literals. This is needed because relay-compiler can't load
 * executable definitions from .graphql files.
 */
object GraphQLWrapper {

  case class Options(outputDir: File, typeScript: Boolean)

  type Results = Set[File]

  def resourceOutputs(resources: Seq[File], options: Options) =
    resources.map(source => source -> resourceOutput(source, options)).toMap

  def resourceOutput(resource: File, options: Options) = {
    val extension = if (options.typeScript) "ts" else "js"
    // Ensure these are absolute otherwise it might mess up the change detection as the files will not equal.
    options.outputDir.getAbsoluteFile / s"${resource.base}.$extension"
  }

  // TODO: Add parallelism.
  /**
   * Wraps the graphql definitions with the graphql interpolator and writes it
   * to a JavaScript/TypeScript file.
   *
   * If there are any collisions then the contents will be appended. It is the
   * callers responsibility to ensure that existing wrappers are deleted
   * beforehand if required.
   */
  def wrapFiles(files: Map[File, File], logger: BuildLogger): Unit =
    files.foreach { case (file, output) =>
      wrapFile(file, output, logger)
    }

  private def wrapFile(file: File, output: File, logger: BuildLogger): Unit = {
    logger.debug(s"Wrapping graphql definitions: $file -> $output")
    fileWriter(StandardCharsets.UTF_8, append = true)(output) { writer =>
      // TODO: We could use the caliban fastparse parsers directly but this seems fast enough for now.
      val documentText = Files.readString(file.toPath, StandardCharsets.UTF_8)
      val document     = Parser.parseQuery(documentText).toTry.get
      document.definitions.foreach { definition =>
        writeWrapper(writer, definition, document, logger)
      }
    }
  }

  /**
   * Write a single GraphQL definition wrapped in a graphql template literal.
   */
  private def writeWrapper(
    writer: BufferedWriter,
    definition: Definition,
    document: Document,
    logger: BuildLogger
  ): Unit = {
    writer.write("graphql`\n")
    val rendered = renderDefinition(definition, document)
    writer.write(escape(trimBlankLines(rendered)))
    writer.write("`\n\n")
  }

  /**
   * Get the name of a GraphQL definition for use as a JavaScript variable name.
   */
  private def getDefinitionName(definition: Definition): String =
    definition match {
      case Definition.ExecutableDefinition.OperationDefinition(opType, name, _, _, _) =>
        name.getOrElse(s"Anonymous${opType.toString.capitalize}")
      case Definition.ExecutableDefinition.FragmentDefinition(name, _, _, _)          =>
        name
      case _                                                                          => ""
    }

  /**
   * Render a GraphQL definition to string format.
   */
  private def renderDefinition(definition: Definition, document: Document): String =
    trimBlankLines(DocumentRenderer.render(Document(List(definition), document.sourceMapper)))

  /**
   * Escape special characters for JavaScript template literals.
   */
  private def escape(s: String): String =
    s.replace("\\", "\\\\")
      .replace("`", "\\`")
      .replaceAll("\\$(?=\\{.*?})", "\\\\\\$")

  /**
   * Remove blank lines from the beginning and end of a string.
   */
  private def trimBlankLines(s: String): String =
    s.split("\n").dropWhile(_.trim.isEmpty).reverse.dropWhile(_.trim.isEmpty).reverse.mkString("\n")
}
