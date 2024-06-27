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

  type Results = Set[File]

  private type Extracts = Map[File, File]

  private final case class Analysis(version: Int, extracts: Extracts)

  private object Analysis {
    def empty: Analysis = Analysis(Version, Map.empty)

    //noinspection TypeAnnotation
    implicit val analysisIso = LList.iso[Analysis, Int :*: Extracts :*: LNil]( //
      { a: Analysis => //
        ("version" -> a.version) :*: ("extracts" -> a.extracts) :*: LNil
      }, {
        case (_, version) :*: (_, extracts) :*: LNil => //
          Analysis(version, extracts)
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

  def extract(cacheStoreFactory: CacheStoreFactory, sources: Set[File], outputDir: File, log: Logger): Results = {
    val stores = Stores(cacheStoreFactory)
    val prevTracker = Tracked.lastOutput[Unit, Analysis](stores.last) { (_, maybePreviousAnalysis) =>
      val previousAnalysis = maybePreviousAnalysis.getOrElse(Analysis.empty)
      log.debug(s"Previous Analysis:\n$previousAnalysis")
      // NOTE: Update clean if you change this.
      Tracked.diffInputs(stores.sources, FileInfo.lastModified)(sources) { sourcesReport =>
        log.debug(s"Source report:\n$sourcesReport")
        // There are 5 cases to handle:
        // 1) Version changed - delete all previous extracts and re-generate everything
        // 2) Source removed - delete the extract
        // 3) Source added - generate the extract
        // 4) Source modified - generate the extract
        // 5) Extract modified - generate the extract
        val (modifiedExtracts, unmodifiedExtracts) = extractModified(sourcesReport, previousAnalysis, outputDir, log)
        val modifiedOutputs                        = modifiedExtracts.values.toSet
        val unmodifiedOutputs                      = unmodifiedExtracts.values.toSet
        val outputs                                = modifiedOutputs ++ unmodifiedOutputs
        // NOTE: Update clean if you change this.
        Tracked.diffOutputs(stores.outputs, FileInfo.lastModified)(outputs) { outputsReport =>
          log.debug(s"Outputs report:\n$outputsReport")
          val unexpectedChanges = unmodifiedOutputs -- outputsReport.unmodified
          if (unexpectedChanges.nonEmpty) {
            val needsExtraction = unmodifiedExtracts.collect {
              case (source, output) if unexpectedChanges.contains(output) => source
            }
            extractFiles(needsExtraction, outputDir, log)
          }
        }
        val extracts = modifiedExtracts ++ unmodifiedExtracts
        Analysis(Version, extracts)
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
    outputDir: File,
    log: Logger
  ): (Extracts, Extracts) = {
    if (Version == previousAnalysis.version) {
      log.debug("Version has not changed")
      val outputsOfRemoved = previousAnalysis.extracts.filterKeys(sourceReport.removed.contains).values
      IO.delete(outputsOfRemoved)
      val addedOrChangedSources = sourceReport.modified -- sourceReport.removed
      val modifiedExtracts      = extractFiles(addedOrChangedSources, outputDir, log)
      val unmodifiedExtracts    = previousAnalysis.extracts.filterKeys(sourceReport.unmodified.contains)
      (modifiedExtracts, unmodifiedExtracts)
    } else {
      log.debug("Version changed")
      IO.delete(previousAnalysis.extracts.values)
      val modifiedExtracts = extractFiles(sourceReport.checked, outputDir, log)
      (modifiedExtracts, Map.empty)
    }
  }

  private def extractFiles(files: Iterable[File], outputDir: File, log: Logger): Extracts =
    files.flatMap { file =>
      extractFile(file, outputDir, log).map(file -> _)
    }.toMap

  private def extractFile(file: File, outputDir: File, log: Logger): Option[File] = {
    log.debug(s"Checking file for graphql definitions: $file")
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
        log.error(
          s"Found a @graphql annotation with the wrong number or type of arguments. It must have exactly one string literal."
        )
        log.error(s"    at ${positionText(pos)}")
      // The application has to be exactly this. It cannot be an alias or qualified.
      // We could support more but it would require SemanticDB which is slower.
      case q"graphqlGen(${t: Lit.String})" =>
        builder += t.value
      case app @ q"graphqlGen(...$exprss)" =>
        def pos = exprss.flatMap(_.headOption).headOption.getOrElse(app).pos
        log.error(
          s"Found a graphqlGen application with the wrong number or type of arguments. It must have exactly one string literal."
        )
        log.error(s"    at ${positionText(pos)}")
    }
    val definitions = builder.result()
    if (definitions.nonEmpty) {
      // relay-compiler doesn't seem to support loading executable definitions from .graphql files.
      // We have to write them to the same language file that we relay-compiler will output to.
      // See https://github.com/facebook/relay/issues/4726#issuecomment-2193708623.
      // TODO: TypeScript output is temporary.
      val outputFile = outputDir / s"${file.base}.ts"
      fileWriter(StandardCharsets.UTF_8)(outputFile) { writer =>
        definitions.foreach { definition =>
          writer.write("graphql`\n")
          writer.write(definition)
          writer.write("\n`\n")
        }
      }
      log.debug(s"Extracted ${definitions.size} definitions to: $outputFile")
      Some(outputFile)
    } else {
      log.debug("No definitions found")
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
