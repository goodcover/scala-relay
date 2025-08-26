package com.goodcover.relay.build

import com.goodcover.relay.build.FileOps._

import java.io._
import java.nio.charset.{Charset, StandardCharsets}
import scala.meta._
import scala.meta.inputs.Input
import scala.util.Try

/**
  * Extracts the GraphQL definitions from @graphql annotations and graphqlGen macros within Scala sources.
  * This is a build-tool agnostic version that can be used by both SBT and Mill.
  */
object GraphQLExtractor {

  // Increment when the code changes to bust the cache.
  private val Version = 1

  final case class Options(outputDir: File, dialect: Dialect) {
    override def equals(obj: Any): Boolean = obj match {
      case Options(otherOutputDir, otherDialect) =>
        outputDir == otherOutputDir && dialect.isEquivalentTo(otherDialect)
      case _ => false
    }
  }

  type Results = Map[File, File]

  private def sourceOutputs(sources: Seq[File], options: Options) =
    sources.map(source => source -> sourceOutput(source, options)).toMap

  private def sourceOutput(source: File, options: Options) =
    // Ensure these are absolute otherwise it might mess up the change detection as the files will not equal.
    options.outputDir.getAbsoluteFile / s"${source.base}.graphql"

  /**
    * Extract GraphQL definitions from source files without caching.
    * This is a simplified version for build tools that handle their own caching.
    */
  def extractSimple(sources: Set[File], options: Options, logger: BuildLogger): Results = {
    logger.debug("Running GraphqlExtractor...")

    val extracts = sources.flatMap { source =>
      val out    = sourceOutput(source, options)
      val pair   = source -> out
      val result = extractFile(source, out, options.dialect, logger)

      Seq(pair)
    }.toMap

    extracts
  }

  // TODO: Add parallelism.

  /**
    * Extracts the graphql definitions from the files.
    *
    * If there are any collisions then the contents will be appended. It is the callers responsibility to ensure that
    * existing extracts are deleted beforehand if required.
    */
  def extractFiles(files: Map[File, File], dialect: Dialect, logger: BuildLogger): Map[File, File] =
    files.filter {
      case (file, output) =>
        Try(extractFile(file, output, dialect, logger)).fold({ t =>
          logger.warn(
            "Failed to check file. File will be ignored. Please ensure that relayExtractDialect has the correct value."
          )
          logger.warn(t.getMessage)
          false
        }, identity)
    }

  /**
    * Extract GraphQL definitions from a single source file.
    */
  private def extractFile(file: File, output: File, dialect: Dialect, logger: BuildLogger): Boolean = {
    logger.debug(s"Checking file for graphql definitions: $file")
    val input   = Input.File(file)
    val source  = input.parse[Source](implicitly, implicitly, dialect).get
    val builder = Iterable.newBuilder[String]

    def extractFromTree(tree: Tree): Unit = {
      tree match {
        // The annotation has to be exactly this. It cannot be an alias or qualified.
        // We could support more but it would require SemanticDB which is slower.
        case mod"@graphql(${t: Lit.String})" =>
          builder += t.value
        case annot @ mod"@graphql(...$exprss)" =>
          def pos = exprss.flatMap(_.headOption).headOption.getOrElse(annot).pos

          logger.error(
            s"Found a @graphql annotation with the wrong number or type of arguments. It must have exactly one string literal."
          )
          logger.error(s"    at ${positionText(pos)}")
          logger.debug(annot.structure)
          logger.debug(exprss.toString)
        case q"graphqlGen(${t: Lit.String})" =>
          builder += t.value
        case q"${_}.graphqlGen(${t: Lit.String})" =>
          builder += t.value
        case app @ q"graphqlGen(...$exprss)" =>
          // Term.Name("graphqlGen") also matches this. Ignore it.
          if (!app.isInstanceOf[Term.Name]) {
            def pos = exprss.flatMap(_.headOption).headOption.getOrElse(app).pos

            logger.error(
              s"Found a graphqlGen application with the wrong number or type of arguments. It must have exactly one string literal."
            )
            logger.error(s"    at ${positionText(pos)}")
            logger.debug(app.structure)
            logger.debug(exprss.toString)
          }
        case _ =>
          // Recursively traverse children
          tree.children.foreach(extractFromTree)
      }
    }

    extractFromTree(source)

    val definitions = builder.result()
    if (definitions.nonEmpty) {
      writeGraphql(file, output, definitions)
      logger.debug(s"Extracted ${definitions.size} definitions to: $output")
      true
    } else {
      logger.debug("No definitions found")
      false
    }
  }

  private def writeGraphql(source: File, output: File, definitions: Iterable[String]): Unit = {
    // Ensure these are absolute otherwise it might mess up the change detection since it uses hash codes.
    fileWriter(StandardCharsets.UTF_8, append = true)(output) { writer =>
      definitions.foreach { definition =>
        writer.write("# Extracted from ")
        writer.write(source.getAbsolutePath)
        writer.write('\n')
        val trimmed = trimBlankLines(definition)
        val lines   = trimmed.linesIterator
        val prefix = {
          if (lines.hasNext) {
            val firstLine = lines.next()
            val indent    = firstLine.takeWhile(_.isWhitespace)
            writer.write(firstLine.drop(indent.length))
            writer.write('\n')
            indent
          } else ""
        }
        lines.foreach { line =>
          writer.write(removeLongestPrefix(line, prefix))
          writer.write('\n')
        }
        writer.write('\n')
      }
    }
  }

  private def removeLongestPrefix(s: String, prefix: String): String = {
    val n = prefix.indices
      .find { i =>
        i < s.length && s.charAt(i) != prefix.charAt(i)
      }
      .getOrElse(prefix.length)
    s.drop(n)
  }

  private def positionText(position: Position): String = {
    position.input match {
      case Input.File(path, _)        => s"${path.toString}:${position.startLine}:${position.startColumn}"
      case Input.VirtualFile(path, _) => s"$path:${position.startLine}:${position.startColumn}"
      case _                          => position.toString
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
