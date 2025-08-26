package com.goodcover.relay.build

import java.io.{
  BufferedReader,
  BufferedWriter,
  File,
  FileInputStream,
  FileOutputStream,
  FileWriter,
  IOException,
  InputStreamReader,
  OutputStreamWriter
}
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.Files
import scala.meta._
import scala.meta.inputs.Input

// File operations helper
object FileOps {
  implicit class FileOps(file: File) {
    def /(child: String): File = new File(file, child)
  }

  private def closeCloseable[T <: AutoCloseable]: T => Unit = _.close()

  def file[T <: AutoCloseable](openF: File => T): OpenFile[T] = file(openF, closeCloseable)

  def file[T](openF: File => T, closeF: T => Unit): OpenFile[T] =
    new OpenFile[T] {
      def openImpl(file: File) = openF(file)
      def close(t: T)          = closeF(t)
    }

  def fileWriter(charset: Charset = StandardCharsets.UTF_8, append: Boolean = false) =
    file(f => new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, append), charset)))

  def fileReader(charset: Charset) =
    file(f => new BufferedReader(new InputStreamReader(new FileInputStream(f), charset)))

  trait OpenFile[T] extends Using[File, T] {
    protected def openImpl(file: File): T
    protected final def open(file: File): T = {
      val parent = file.getParentFile
      if (parent != null) {
        try Files.createDirectory(parent.toPath)
        catch { case _: IOException => }
      }
      openImpl(file)
    }
  }

  abstract class Using[Source, T] {
    protected def open(src: Source): T
    def apply[R](src: Source)(f: T => R): R = {
      val resource = open(src)
      try {
        f(resource)
      } finally {
        close(resource)
      }
    }
    protected def close(out: T): Unit
  }
}

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
      val input   = Input.File(file)
      val source  = input.parse[Source](implicitly, implicitly, dialect).get
      val builder = collection.mutable.ListBuffer[String]()

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
            }
          case _ =>
            // Recursively traverse children
            tree.children.foreach(extractFromTree)
        }
      }

      extractFromTree(source)

      if (builder.nonEmpty) {
        val output = new File(outputDir, file.getName.stripSuffix(".scala") + ".graphql")
        outputDir.mkdirs()

        writeGraphql(file, output, builder.toList)

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

  private def writeGraphql(source: File, output: File, definitions: List[String]): Unit = {
    val writer = new FileWriter(output, StandardCharsets.UTF_8, false) // Don't append, overwrite
    try {
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
    } finally {
      writer.close()
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

  private def trimBlankLines(s: String): String = {
    val lines   = s.linesIterator.toList
    val trimmed = lines.dropWhile(_.trim.isEmpty).reverse.dropWhile(_.trim.isEmpty).reverse
    trimmed.mkString("\n")
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
