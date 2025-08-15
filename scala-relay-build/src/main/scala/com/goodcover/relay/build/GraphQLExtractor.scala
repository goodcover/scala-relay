package com.goodcover.relay.build

import java.io.{File, FileWriter}
import java.nio.charset.StandardCharsets
import scala.meta._
import scala.meta.inputs.Input
import scala.meta.internal.inputs.XtensionInput
import scala.util.Try

// File operations helper
object FileOps {
  implicit class FileOps(file: File) {
    def /(child: String): File = new File(file, child)
  }
}

/**
 * Extracts the GraphQL definitions from @graphql annotations and graphqlGen macros within Scala sources.
 * This is a build-tool agnostic version that can be used by both SBT and Mill.
 */
object GraphQLExtractor {
  import FileOps._

  // Increment when the code changes to bust the cache.
  private val Version = 1

  final case class Options(outputDir: File, dialect: Dialect) {
    override def equals(obj: Any): Boolean = obj match {
      case Options(otherOutputDir, otherDialect) =>
        outputDir == otherOutputDir && dialect.isEquivalentTo(otherDialect)
      case _ => false
    }
  }

  type Results = Set[File]

  /**
   * Extract GraphQL definitions from source files without caching.
   * This is a simplified version for build tools that handle their own caching.
   */
  def extractSimple(sources: Set[File], options: Options, logger: BuildLogger): Results = {
    logger.debug("Running GraphqlExtractor...")

    val extracts = sources.flatMap { source =>
      extractFile(source, options.outputDir, options.dialect, logger)
    }

    extracts
  }

  /**
   * Extract GraphQL definitions from a single source file.
   */
  private def extractFile(file: File, outputDir: File, dialect: Dialect, logger: BuildLogger): Option[File] = {
    logger.debug(s"Checking file for graphql definitions: $file")

    try {
      val input = Input.File(file)
      val source = input.parse[Source](implicitly, implicitly, dialect).get
      val builder = collection.mutable.ListBuffer[String]()

      def extractFromTree(tree: Tree): Unit = tree match {
        // The annotation has to be exactly this. It cannot be an alias or qualified.
        // We could support more but it would require SemanticDB which is slower.
        case mod"@graphql(${t: Lit.String})" =>
          builder += t.value
        case annot @ mod"@graphql(...$exprss)" =>
          def pos = exprss.flatMap(_.headOption).headOption.getOrElse(annot).pos
          logger.error(
            s"@graphql annotation must have exactly one string literal argument at ${pos.input.syntax}:${pos.startLine + 1}:${pos.startColumn + 1}"
          )
        case q"graphqlGen(${t: Lit.String})" =>
          builder += t.value
        case q"graphqlGen(...$exprss)" =>
          def pos = exprss.flatMap(_.headOption).headOption.getOrElse(q"graphqlGen").pos
          logger.error(
            s"graphqlGen macro must have exactly one string literal argument at ${pos.input.syntax}:${pos.startLine + 1}:${pos.startColumn + 1}"
          )
        case _ =>
          tree.children.foreach(extractFromTree)
      }

      extractFromTree(source)

      if (builder.nonEmpty) {
        val output = new File(outputDir, file.getName.stripSuffix(".scala") + ".graphql")
        outputDir.mkdirs()

        val writer = new FileWriter(output, StandardCharsets.UTF_8)
        try {
          builder.foreach { graphql =>
            writer.write(graphql)
            writer.write("\n\n")
          }
        } finally {
          writer.close()
        }

        logger.debug(s"Extracted ${builder.size} GraphQL definitions from $file to $output")
        Some(output)
      } else {
        None
      }
    } catch {
      case e: Exception =>
        logger.error(s"Failed to extract GraphQL from $file: ${e.getMessage}")
        None
    }
  }

  /**
   * Clean up extracted files
   */
  def clean(outputDir: File): Unit = {
    if (outputDir.exists()) {
      outputDir.listFiles().foreach { file =>
        if (file.getName.endsWith(".graphql")) {
          val _ = file.delete()
        }
      }
    }
  }
}
