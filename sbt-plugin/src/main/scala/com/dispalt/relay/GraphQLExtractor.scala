package com.dispalt.relay

import sbt._
import sbt.io.Using.fileWriter
import sbt.util.CacheImplicits.{mapFormat => _, _}
import sbt.util.{CacheStore, CacheStoreFactory}
import sjsonnew._

import java.nio.charset.StandardCharsets
import scala.meta._
import scala.meta.inputs.Input

/**
  * Extracts the GraphQL definitions from @graphql annotations and graphqlGen macros within Scala sources.
  */
object GraphQLExtractor {

  // Increment when the code changes to bust the cache.
  private val Version = 1

  final case class Options(outputDir: File)

  object Options {
    //noinspection TypeAnnotation
    implicit val iso = LList.iso[Options, File :*: LNil]( //
      { o: Options => //
        ("outputDir" -> o.outputDir) :*: LNil
      }, {
        case (_, outputDir) :*: LNil => //
          Options(outputDir)
      }
    )
  }

  type Results = Set[File]

  private type Extracts = Map[File, File]

  // TODO: Get rid of extracts.
  private final case class Analysis(version: Int, options: Options, extracts: Extracts)

  private object Analysis {
    def apply(options: Options): Analysis = Analysis(Version, options, Map.empty)

    //noinspection TypeAnnotation
    implicit val iso = LList.iso[Analysis, Int :*: Options :*: Extracts :*: LNil]( //
      { a: Analysis => //
        ("version" -> a.version) :*: ("options" -> a.options) :*: ("extracts" -> a.extracts) :*: LNil
      }, {
        case (_, version) :*: (_, options) :*: (_, extracts) :*: LNil => //
          Analysis(version, options, extracts)
      }
    )
  }

  private final case class Stores(last: CacheStore, sources: CacheStore, outputs: CacheStore)

  private object Stores {
    def apply(cacheStoreFactory: CacheStoreFactory): Stores = Stores(
      last = cacheStoreFactory.make("last"),
      sources = cacheStoreFactory.make("sources"),
      outputs = cacheStoreFactory.make("outputs")
    )
  }

  def extract(cacheStoreFactory: CacheStoreFactory, sources: Set[File], options: Options, logger: Logger): Results = {
    logger.debug("Running GraphqlExtractor...")
    val stores = Stores(cacheStoreFactory)
    val prevTracker = Tracked.lastOutput[Unit, Analysis](stores.last) { (_, maybePreviousAnalysis) =>
      val previousAnalysis = maybePreviousAnalysis.getOrElse(Analysis(options))
      logger.debug(s"Previous analysis:\n$maybePreviousAnalysis")
      // NOTE: Update clean if you change this.
      Tracked.diffInputs(stores.sources, FileInfo.lastModified)(sources) { sourcesReport =>
        logger.debug(s"Sources:\n$sourcesReport")
        // There are 5 cases to handle:
        // 1) Version or options changed - delete all previous extracts and re-extract everything
        // 2) Source removed - delete the extract
        // 3) Source added - generate the extract
        // 4) Source modified - generate the extract
        // 5) Extract modified - generate the extract
        val (modifiedExtracts, unmodifiedExtracts) = extractModified(sourcesReport, previousAnalysis, options, logger)
        val modifiedOutputs                        = modifiedExtracts.values.toSet
        val unmodifiedOutputs                      = unmodifiedExtracts.values.toSet
        val outputs                                = modifiedOutputs ++ unmodifiedOutputs
        // NOTE: Update clean if you change this.
        Tracked.diffOutputs(stores.outputs, FileInfo.lastModified)(outputs) { outputsReport =>
          logger.debug(s"Outputs:\n$outputsReport")
          val unexpectedChanges = unmodifiedOutputs -- outputsReport.unmodified
          if (unexpectedChanges.nonEmpty) {
            logger.warn("Unexpected modifications found to files:")
            unexpectedChanges.foreach { file =>
              logger.warn(s" ${file.absolutePath}")
            }
            logger.warn("Ensure that nothing is modifying these files so as to get the most benefit from the cache.")
            val outputSources   = invertOneToOne(previousAnalysis.extracts)
            val needsExtraction = unexpectedChanges.foldLeft(Map.empty[File, File]) {
              case (acc, output) => acc ++ outputSources.getOrElse(output, Vector.empty).map(_ -> output)
            }
            // Don't forget to delete the old ones since extract appends.
            IO.delete(unexpectedChanges)
            val unexpectedExtracts = extractFiles(needsExtraction, logger)
            logger.warn(s"Extracted an additional ${unexpectedExtracts.size} GraphQL documents.")
            unexpectedExtracts
          }
        }
        val extracts = modifiedExtracts ++ unmodifiedExtracts
        Analysis(Version, options, extracts)
      }
    }
    prevTracker(()).extracts.values.toSet
  }

  /**
    * If the version has changed this will delete all previous extracts and re-extract everything. Otherwise it will
    * remove old extracts for removed sources and extract from anything that is new or was modified.
    *
    * @return a tuple where the first element are the files that were extracted and the second are all the extracts
    *         from the previous analysis that have not been modified
    */
  private def extractModified(
    sourceReport: ChangeReport[File],
    previousAnalysis: Analysis,
    options: Options,
    logger: Logger
  ): (Extracts, Extracts) = {
    def versionUnchanged = Version == previousAnalysis.version
    def optionsUnchanged = options == previousAnalysis.options
    if (versionUnchanged && optionsUnchanged) {
      logger.debug("Version and options have not changed")
      // We have to be careful here because we can have multiple sources extracting to the same output.
      // When something is modified we need to re-extract not just those modifications but also the other sources that
      // extracted to the same output.
      val modifiedOutputs = sourceOutputs(sourceReport.modified.toSeq, options)
      IO.delete(modifiedOutputs.values)
      val unmodifiedSources = invertOneToOne(sourceOutputs(sourceReport.unmodified.toSeq, options))
      val modifiedAndTransitiveOutputs = modifiedOutputs.flatMap {
        case entry @ (_, output) => entry +: unmodifiedSources.getOrElse(output, Vector.empty).map(_ -> output)
      }
      val needsExtracting = modifiedAndTransitiveOutputs.filterKeys(!sourceReport.removed.contains(_))
      if (needsExtracting.nonEmpty) logger.info("Extracting GraphQL...")
      val changedExtracts = extractFiles(needsExtracting, logger)
      if (needsExtracting.nonEmpty) logger.info(s"Extracted ${changedExtracts.size} GraphQL documents.")
      val unchangedExtracts =
        sourceOutputs(sourceReport.unmodified.toSeq, options).filterKeys(!needsExtracting.contains(_))
      (changedExtracts, unchangedExtracts)
    } else {
      if (!versionUnchanged) logger.debug(s"Version changed:\n$Version")
      else logger.debug(s"Options changed:\n$options")
      val previousOutputs = previousAnalysis.extracts.values
      IO.delete(previousOutputs)
      val needsExtraction = sourceOutputs(sourceReport.checked.toSeq, options)
      val changedExtracts = extractFiles(needsExtraction, logger)
      (changedExtracts, Map.empty)
    }
  }

  private def sourceOutputs(sources: Seq[File], options: Options) =
    sources.map(source => source -> sourceOutput(source, options)).toMap

  private def sourceOutput(source: File, options: Options) =
    // Ensure these are absolute otherwise it might mess up the change detection as the files will not equal.
    options.outputDir.getAbsoluteFile / s"${source.base}.graphql"

  // TODO: Add parallelism.
  /**
    * Extracts the graphql definitions from the files.
    *
    * If there are any collisions then the contents will be appended. It is the callers responsibility to ensure that
    * existing extracts are deleted beforehand if required.
    */
  private def extractFiles(files: Map[File, File], logger: Logger): Extracts =
    files.filter {
      case (file, output) => extractFile(file, output, logger)
    }

  private def extractFile(file: File, output: File, logger: Logger): Boolean = {
    logger.debug(s"Checking file for graphql definitions: $file")
    val input   = Input.File(file)
    val source  = input.parse[Source].get
    val builder = Iterable.newBuilder[String]
    source.traverse {
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
        logger.debug(annot.syntax)
        logger.debug(annot.structure)
      // The application has to be exactly this. It cannot be an alias or qualified.
      // We could support more but it would require SemanticDB which is slower.
      case q"graphqlGen(${t: Lit.String})" =>
        builder += t.value
      case app @ q"graphqlGen(...$exprss)" =>
        def pos = exprss.flatMap(_.headOption).headOption.getOrElse(app).pos
        logger.error(
          s"Found a graphqlGen application with the wrong number or type of arguments. It must have exactly one string literal."
        )
        logger.error(s"    at ${positionText(pos)}")
        logger.debug(app.syntax)
        logger.debug(app.structure)
    }
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
        writer.write(source.absolutePath)
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

  private def trimBlankLines(s: String): String =
    s.replaceFirst("""^\s*(\R+|$)""", "").replaceFirst("""\R\s*$""", "")

  private def removeLongestPrefix(s: String, prefix: String): String = {
    val n = prefix.indices
      .find { i =>
        s.charAt(i) != prefix.charAt(i)
      }
      .getOrElse(prefix.length)
    s.drop(n)
  }

  private def positionText(position: Position): String = {
    position.input match {
      case Input.File(path, _)        => s"${path.syntax}:${position.startLine}:${position.startColumn}"
      case Input.VirtualFile(path, _) => s"$path:${position.startLine}:${position.startColumn}"
      case _                          => position.toString
    }
  }

  def clean(cacheStoreFactory: CacheStoreFactory): Unit = {
    val Stores(last, sources, outputs) = Stores(cacheStoreFactory)
    last.delete()
    Tracked.diffInputs(sources, FileInfo.lastModified).clean()
    Tracked.diffOutputs(outputs, FileInfo.lastModified).clean()
  }
}
