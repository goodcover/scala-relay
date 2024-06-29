package com.dispalt.relay

import sbt.*
import sbt.io.Using.fileWriter
import sbt.util.CacheImplicits.*
import sbt.util.{CacheStore, CacheStoreFactory}
import sjsonnew.*

import java.nio.charset.StandardCharsets
import scala.meta.*
import scala.meta.inputs.Input

object GraphqlExtractor {

  // Increment when the extraction code changes to bust the cache.
  private val Version = 1

  final case class Options(outputDir: File, typeScript: Boolean)

  object Options {
    //noinspection TypeAnnotation
    implicit val iso = LList.iso[Options, File :*: Boolean :*: LNil]( //
      { o: Options => //
        ("outputDir" -> o.outputDir) :*: ("typeScript" -> o.typeScript) :*: LNil
      }, {
        case (_, outputDir) :*: (_, typeScript) :*: LNil => //
          Options(outputDir, typeScript)
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

  def extract(
    cacheStoreFactory: CacheStoreFactory,
    sources: Set[File],
    options: Options,
    logger: Logger
  ): Results = {
    val stores = Stores(cacheStoreFactory)
    val prevTracker = Tracked.lastOutput[Unit, Analysis](stores.last) { (_, maybePreviousAnalysis) =>
      val previousAnalysis = maybePreviousAnalysis.getOrElse(Analysis(options))
      logger.debug(s"Previous Analysis:\n$previousAnalysis")
      // NOTE: Update clean if you change this.
      Tracked.diffInputs(stores.sources, FileInfo.lastModified)(sources) { sourcesReport =>
        logger.debug(s"Source report:\n$sourcesReport")
        // There are 5 cases to handle:
        // 1) Version or options changed - delete all previous extracts and re-generate everything
        // 2) Source removed - delete the extract
        // 3) Source added - generate the extract
        // 4) Source modified - generate the extract
        // 5) Extract modified - generate the extract
        val (modifiedExtracts, unmodifiedExtracts) = extractModified(sourcesReport, previousAnalysis, options, logger)
        val modifiedOutputs   = modifiedExtracts.values.toSet
        val unmodifiedOutputs = unmodifiedExtracts.values.toSet
        val outputs           = modifiedOutputs ++ unmodifiedOutputs
        // NOTE: Update clean if you change this.
        Tracked.diffOutputs(stores.outputs, FileInfo.lastModified)(outputs) { outputsReport =>
          logger.debug(s"Outputs report:\n$outputsReport")
          val unexpectedChanges = unmodifiedOutputs -- outputsReport.unmodified
          if (unexpectedChanges.nonEmpty) {
            val needsExtraction = unmodifiedExtracts.collect {
              case (source, output) if unexpectedChanges.contains(output) => source
            }
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
    if (Version == previousAnalysis.version && options == previousAnalysis.options) {
      logger.debug("Version and options have not changed")
      val outputsOfRemoved = previousAnalysis.extracts.filterKeys(sourceReport.removed.contains).values
      IO.delete(outputsOfRemoved)
      val addedOrChangedSources = sourceReport.modified -- sourceReport.removed
      val modifiedExtracts      = extractFiles(addedOrChangedSources, options, logger)
      val unmodifiedExtracts    = previousAnalysis.extracts.filterKeys(sourceReport.unmodified.contains)
      (modifiedExtracts, unmodifiedExtracts)
    } else {
      logger.debug((if (Version != previousAnalysis.version) "Version" else "Options") + " changed")
      IO.delete(previousAnalysis.extracts.values)
      val modifiedExtracts = extractFiles(sourceReport.checked, options, logger)
      (modifiedExtracts, Map.empty)
    }
  }

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
    }
    val definitions = builder.result()
    if (definitions.nonEmpty) {
      // relay-compiler doesn't seem to support loading executable definitions from .graphql files.
      // We have to write them to the same language file that we relay-compiler will output to.
      // See https://github.com/facebook/relay/issues/4726#issuecomment-2193708623.
      val extension  = if (options.typeScript) "ts" else "js"
      val outputFile = options.outputDir / s"${file.base}.$extension"
      fileWriter(StandardCharsets.UTF_8)(outputFile) { writer =>
        definitions.foreach { definition =>
          writer.write("graphql`\n")
          writer.write(definition)
          writer.write("\n`\n")
        }
      }
      logger.debug(s"Extracted ${definitions.size} definitions to: $outputFile")
      Some(outputFile)
    } else {
      logger.debug("No definitions found")
      None
    }
  }

  private def positionText(position: Position): String = {
    position.input match {
      case Input.File(path, _)        => s"${path.syntax}:${position.start}:${position.startColumn}"
      case Input.VirtualFile(path, _) => s"$path:${position.start}:${position.startColumn}"
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
