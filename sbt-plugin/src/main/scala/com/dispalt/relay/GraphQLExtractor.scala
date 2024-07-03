package com.dispalt.relay

import sbt.*
import sbt.io.Using.fileWriter
import sbt.util.CacheImplicits.*
import sbt.util.{CacheStore, CacheStoreFactory}
import sjsonnew.*

import java.nio.charset.StandardCharsets
import scala.meta.*
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
    logger.info("Extracting GraphQL...")
    val stores = Stores(cacheStoreFactory)
    val prevTracker = Tracked.lastOutput[Unit, Analysis](stores.last) { (_, maybePreviousAnalysis) =>
      val previousAnalysis = maybePreviousAnalysis.getOrElse(Analysis(options))
      logger.debug(s"Previous analysis:\n$previousAnalysis")
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
            val inverse         = invertOneToOne(previousAnalysis.extracts)
            val needsExtraction = unexpectedChanges.flatMap(inverse.get).flatten
            // Don't forget to delete the old ones since extract appends.
            IO.delete(unexpectedChanges)
            extractFiles(needsExtraction, options, logger)
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
      val outputsOfRemoved = previousAnalysis.extracts.filterKeys(sourceReport.removed.contains).values
      IO.delete(outputsOfRemoved)
      val addedOrChangedSources = sourceReport.modified -- sourceReport.removed
      // We can have multiple sources extracting to the same file since the output structure is flat.
      // So we need to delete any previous extracts and then append during extraction.
      addedOrChangedSources.foreach { source =>
        previousAnalysis.extracts.get(source).foreach(IO.delete)
      }
      val modifiedExtracts   = extractFiles(addedOrChangedSources, options, logger)
      val unmodifiedExtracts = previousAnalysis.extracts.filterKeys(sourceReport.unmodified.contains)
      (modifiedExtracts, unmodifiedExtracts)
    } else {
      def whatChanged =
        if (!versionUnchanged) "Version"
        else "Options"
      logger.debug(s"$whatChanged changed")
      val previousOutputs = previousAnalysis.extracts.values
      IO.delete(previousOutputs)
      val modifiedExtracts = extractFiles(sourceReport.checked, options, logger)
      (modifiedExtracts, Map.empty)
    }
  }

  // TODO: Add parallelism.
  /**
    * Extracts the graphql definitions from the files.
    *
    * If there are any collisions then the contents will be appended. It is the callers responsibility to ensure that
    * existing extracts are deleted beforehand if required.
    */
  private def extractFiles(files: Iterable[File], options: Options, logger: Logger): Extracts =
    files.flatMap { file =>
      extractFile(file, options, logger).map(file -> _)
    }.toMap

  private def extractFile(file: File, options: Options, logger: Logger): Option[File] = {
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
      val graphqlFile = writeGraphql(file, options, definitions)
      logger.debug(s"Extracted ${definitions.size} definitions to: $graphqlFile")
      Some(graphqlFile)
    } else {
      logger.debug("No definitions found")
      None
    }
  }

  private def writeGraphql(source: File, options: Options, definitions: Iterable[String]): File = {
    // Ensure these are absolute otherwise it might mess up the change detection since it uses hash codes.
    val graphqlFile = options.outputDir.getAbsoluteFile / s"${source.base}.graphql"
    fileWriter(StandardCharsets.UTF_8, append = true)(graphqlFile) { writer =>
      definitions.zipWithIndex.foreach {
        case (definition, i) =>
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
    graphqlFile
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
