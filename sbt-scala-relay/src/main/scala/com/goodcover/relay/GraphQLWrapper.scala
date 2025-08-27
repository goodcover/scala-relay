package com.goodcover.relay

import com.goodcover.relay.build.{GraphQLWrapper => BuildGraphQLWrapper}
import sbt._
import sbt.util.CacheImplicits._
import sbt.util.{CacheStore, CacheStoreFactory}
import sjsonnew._

object GraphQLWrapper {

  // This should be temporary. It exists only to workaround a stupid limitation of relay-compiler where it doesn't load
  // definitions from GraphQL files.

  // TODO: Report this as a separate issue.
  //  relay-compiler doesn't seem to support loading executable definitions from .graphql files.
  //  We have to write them to the same language file that we relay-compiler will output to.
  //  See https://github.com/facebook/relay/issues/4726#issuecomment-2193708623.

  // Increment when the code changes to bust the cache.
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

  private type Wrappers = Map[File, File]

  // TODO: Get rid of wrappers.
  private final case class Analysis(version: Int, options: Options, wrappers: Wrappers)

  private object Analysis {
    def apply(options: Options): Analysis = Analysis(Version, options, Map.empty)

    //noinspection TypeAnnotation
    implicit val iso = LList.iso[Analysis, Int :*: Options :*: Wrappers :*: LNil]( //
      { a: Analysis => //
        ("version" -> a.version) :*: ("options" -> a.options) :*: ("wrappers" -> a.wrappers) :*: LNil
      }, {
        case (_, version) :*: (_, options) :*: (_, wrappers) :*: LNil => //
          Analysis(version, options, wrappers)
      }
    )
  }

  private final case class Stores(last: CacheStore, resources: CacheStore, outputs: CacheStore)

  private object Stores {
    def apply(cacheStoreFactory: CacheStoreFactory): Stores = Stores(
      last = cacheStoreFactory.make("last"),
      resources = cacheStoreFactory.make("resources"),
      outputs = cacheStoreFactory.make("outputs")
    )
  }

  def wrap(cacheStoreFactory: CacheStoreFactory, sources: Set[File], options: Options, logger: Logger): Results = {
    logger.debug("Running GraphqlWrapper...")
    val stores = Stores(cacheStoreFactory)
    val prevTracker = Tracked.lastOutput[Unit, Analysis](stores.last) { (_, maybePreviousAnalysis) =>
      val previousAnalysis = maybePreviousAnalysis.getOrElse(Analysis(options))
      logger.debug(s"Previous analysis:\n$maybePreviousAnalysis")
      // NOTE: Update clean if you change this.
      Tracked.diffInputs(stores.resources, FileInfo.lastModified)(sources) { resourcesReport =>
        logger.debug(s"Resources:\n$resourcesReport")
        // There are 5 cases to handle:
        // 1) Version or options changed - delete all previous wrapper and re-wrap everything
        // 2) Resource removed - delete the wrapper
        // 3) Resource added - generate the wrapper
        // 4) Resource modified - generate the wrapper
        // 5) Wrapper modified - generate the wrapper
        val (modifiedWrappers, unmodifiedWrappers) = wrapModified(resourcesReport, previousAnalysis, options, logger)
        val modifiedOutputs                        = modifiedWrappers.values.toSet
        val unmodifiedOutputs                      = unmodifiedWrappers.values.toSet
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
            val outputSources = invertOneToOne(previousAnalysis.wrappers)
            val needsWrapping = unexpectedChanges.foldLeft(Map.empty[File, File]) {
              case (acc, output) => acc ++ outputSources.getOrElse(output, Vector.empty).map(_ -> output)
            }
            // Don't forget to delete the old ones since wrap appends.
            IO.delete(needsWrapping.values)
            wrapFiles(needsWrapping, logger)
            logger.warn(s"Wrapped an additional ${needsWrapping.size} GraphQL definitions.")
            needsWrapping
          }
        }
        val wrappers = modifiedWrappers ++ unmodifiedWrappers
        Analysis(Version, options, wrappers)
      }
    }
    prevTracker(()).wrappers.values.toSet
  }

  /**
    * If the version has changed this will delete all previous wrappers and re-wrap everything. Otherwise it will
    * remove old wrapper for removed resources and wrap anything that is new or was modified.
    *
    * @return a tuple where the first element are the new wrappers and the second are the wrappers from the previous
    *         analysis that have not been modified
    */
  private def wrapModified(
    resourceReport: ChangeReport[File],
    previousAnalysis: Analysis,
    options: Options,
    logger: Logger
  ): (Wrappers, Wrappers) = {
    def versionUnchanged = Version == previousAnalysis.version
    def optionsUnchanged = options == previousAnalysis.options
    if (versionUnchanged && optionsUnchanged) {
      logger.debug("Version and options have not changed")
      // We have to be careful here because we can have multiple resources wrapping to the same output.
      // When something is modified we need to re-wrap not just those modifications but also the other resources that
      // wrap to the same output.
      val modifiedOutputs = resourceOutputs(resourceReport.modified.toSeq, options)
      IO.delete(modifiedOutputs.values)
      val unmodifiedResources = invertOneToOne(resourceOutputs(resourceReport.unmodified.toSeq, options))
      val modifiedAndTransitiveOutputs = modifiedOutputs.flatMap {
        case entry @ (_, output) => entry +: unmodifiedResources.getOrElse(output, Vector.empty).map(_ -> output)
      }
      val needsWrapping = modifiedAndTransitiveOutputs.filterKeys(!resourceReport.removed.contains(_))
      needsWrapping.foreach {
        case (resource, output) if output.exists() =>
          logger.warn(s"BUG: Output ${output.getPath} of resource ${resource.getPath} should not exist.")
        case _ => ()
      }
      if (needsWrapping.nonEmpty)
        logger.info(s"Wrapping GraphQL in ${if (options.typeScript) "TypeScript" else "JavaScript"}...")
      wrapFiles(needsWrapping, logger)
      if (needsWrapping.nonEmpty) logger.info(s"Wrapped ${needsWrapping.size} GraphQL documents.")
      val previouslyWrapped = resourceReport.checked -- needsWrapping.keySet
      val unchangedWrappers = previousAnalysis.wrappers.filterKeys(previouslyWrapped.contains)
      (needsWrapping, unchangedWrappers)
    } else {
      if (!versionUnchanged) logger.debug(s"Version changed:\n$Version")
      else logger.debug(s"Options changed:\n$options")
      val previousOutputs = previousAnalysis.wrappers.values
      IO.delete(previousOutputs)
      val needsExtraction = resourceOutputs(resourceReport.checked.toSeq, options)
      IO.delete(needsExtraction.values)
      wrapFiles(needsExtraction, logger)
      (needsExtraction, Map.empty)
    }
  }

  private def resourceOutputs(resources: Seq[File], options: Options) =
    BuildGraphQLWrapper.resourceOutputs(resources, options)

  // TODO: Add parallelism.
  /**
    * Wraps the graphql definitions with the graphql interpolator and writes it to a JavaScript/TypeScript file.
    *
    * If there are any collisions then the contents will be appended. It is the callers responsibility to ensure that
    * existing wrappers are deleted beforehand if required.
    */
  private def wrapFiles(files: Map[File, File], logger: Logger): Unit =
    BuildGraphQLWrapper.wrapFiles(files, SbtBuildLogger(logger))

  def clean(cacheStoreFactory: CacheStoreFactory): Unit = {
    val Stores(last, resources, outputs) = Stores(cacheStoreFactory)
    last.delete()
    Tracked.diffInputs(resources, FileInfo.lastModified).clean()
    Tracked.diffOutputs(outputs, FileInfo.lastModified).clean()
  }
}
