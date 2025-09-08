package com.goodcover.relay

import sbt._
import sbt.io.Using.fileWriter
import sbt.util.CacheImplicits._
import sbt.util.{CacheStore, CacheStoreFactory}
import com.goodcover.relay.build.{GraphQLExtractor as SharedQLExtractor}
import sjsonnew._

import java.nio.charset.StandardCharsets
import scala.meta._
import scala.meta.inputs.Input
import scala.util.Try

/**
  * Extracts the GraphQL definitions from @graphql annotations and graphqlGen macros within Scala sources.
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

  object Options {

    implicit val dialectFormat: JsonFormat[Dialect] = serializableFormat

    //noinspection TypeAnnotation
    implicit val iso = LList.iso[Options, File :*: Dialect :*: LNil]( //
      { o: Options => //
        ("outputDir" -> o.outputDir) :*: ("dialect" -> o.dialect) :*: LNil
      }, {
        case (_, outputDir) :*: (_, dialect) :*: LNil => //
          Options(outputDir, dialect)
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
    val sbtLogger = SbtBuildLogger(logger)
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
            val outputSources = invertOneToOne(previousAnalysis.extracts)
            val needsExtraction = unexpectedChanges.foldLeft(Map.empty[File, File]) {
              case (acc, output) => acc ++ outputSources.getOrElse(output, Vector.empty).map(_ -> output)
            }
            // Don't forget to delete the old ones since extract appends.
            IO.delete(needsExtraction.values)
            val unexpectedExtracts = SharedQLExtractor.extractFiles(needsExtraction, options.dialect, sbtLogger)
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
    val sbtBuildLogger = SbtBuildLogger(logger)

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
      needsExtracting.foreach {
        case (source, output) if output.exists() =>
          logger.warn(s"BUG: Output ${output.getPath} of source ${source.getPath} should not exist.")
        case _ => ()
      }
      val changedExtracts = SharedQLExtractor.extractFiles(needsExtracting, options.dialect, sbtBuildLogger)
      if (needsExtracting.nonEmpty) logger.info(s"Extracted ${changedExtracts.size} GraphQL documents.")
      val previouslyExtracted = sourceReport.checked -- needsExtracting.keySet
      val unchangedExtracts = previousAnalysis.extracts.filterKeys(previouslyExtracted.contains)
      (changedExtracts, unchangedExtracts)
    } else {
      if (!versionUnchanged) logger.debug(s"Version changed:\n$Version")
      else logger.debug(s"Options changed:\n$options")
      val previousOutputs = previousAnalysis.extracts.values
      IO.delete(previousOutputs)
      val needsExtraction = sourceOutputs(sourceReport.checked.toSeq, options)
      IO.delete(needsExtraction.values)
      val changedExtracts = SharedQLExtractor.extractFiles(needsExtraction, options.dialect, sbtBuildLogger)
      (changedExtracts, Map.empty)
    }
  }

  private def sourceOutputs(sources: Seq[File], options: Options) =
    sources.map(source => source -> sourceOutput(source, options)).toMap

  private def sourceOutput(source: File, options: Options) =
    // Ensure these are absolute otherwise it might mess up the change detection as the files will not equal.
    options.outputDir.getAbsoluteFile / s"${source.base}.graphql"


  def clean(cacheStoreFactory: CacheStoreFactory): Unit = {
    val Stores(last, sources, outputs) = Stores(cacheStoreFactory)
    last.delete()
    Tracked.diffInputs(sources, FileInfo.lastModified).clean()
    Tracked.diffOutputs(outputs, FileInfo.lastModified).clean()
  }
}
