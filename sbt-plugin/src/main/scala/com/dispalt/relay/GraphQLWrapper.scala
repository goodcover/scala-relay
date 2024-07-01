package com.dispalt.relay

import com.dispalt.relay.GraphQLText.{countSelectionSetDiff, splitComment}
import sbt.*
import sbt.io.Using.{fileReader, fileWriter}
import sbt.util.CacheImplicits.*
import sbt.util.{CacheStore, CacheStoreFactory}
import sjsonnew.*

import java.io.{BufferedReader, BufferedWriter}
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec

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
    logger.info(s"Wrapping GraphQL in ${if (options.typeScript) "TypeScript" else "JavaScript"}...")
    val stores = Stores(cacheStoreFactory)
    val prevTracker = Tracked.lastOutput[Unit, Analysis](stores.last) { (_, maybePreviousAnalysis) =>
      val previousAnalysis = maybePreviousAnalysis.getOrElse(Analysis(options))
      logger.debug(s"Previous analysis:\n$previousAnalysis")
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
            val inverse = unmodifiedWrappers.map {
              case (resource, wrapper) => wrapper -> resource
            }
            val needsWrapping = unexpectedChanges.flatMap(inverse.get)
            wrapFiles(needsWrapping, options, logger)
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
      val outputsOfRemoved = previousAnalysis.wrappers.filterKeys(resourceReport.removed.contains).values
      IO.delete(outputsOfRemoved)
      val addedOrChangedResources = resourceReport.modified -- resourceReport.removed
      val modifiedWrappers        = wrapFiles(addedOrChangedResources, options, logger)
      val unmodifiedWrappers      = previousAnalysis.wrappers.filterKeys(resourceReport.unmodified.contains)
      (modifiedWrappers, unmodifiedWrappers)
    } else {
      def whatChanged =
        if (!versionUnchanged) "Version"
        else "Options"
      logger.debug(s"$whatChanged changed")
      val previousOutputs = previousAnalysis.wrappers.values
      IO.delete(previousOutputs)
      val modifiedWrappers = wrapFiles(resourceReport.checked, options, logger)
      (modifiedWrappers, Map.empty)
    }
  }

  // TODO: Add parallelism.
  private def wrapFiles(files: Iterable[File], options: Options, logger: Logger): Wrappers =
    files.map { file =>
      file -> wrapFile(file, options, logger)
    }.toMap

  private def wrapFile(file: File, options: Options, logger: Logger): File = {
    logger.debug(s"Wrapping graphql definitions: $file")
    val extension   = if (options.typeScript) "ts" else "js"
    val wrapperFile = options.outputDir / s"${file.base}.$extension"
    fileReader(StandardCharsets.UTF_8)(file) { reader =>
      fileWriter(StandardCharsets.UTF_8)(wrapperFile) { writer =>
        writeWrapper(reader, writer, logger)
      }
    }
    wrapperFile
  }

  private def writeWrapper(reader: BufferedReader, writer: BufferedWriter, logger: Logger): Unit = {
    @tailrec
    /**
      * @param level 0 when not inside a graphql macro
      *              1 when inside a graphql macro
      *              +1 for every open selection set
      */
    def loop(level: Int): Unit = {
      Option(reader.readLine()) match {
        case Some(line) if line.isBlank =>
          writer.write(line)
          writer.write("\n")
          loop(level)
        case Some(line) =>
          if (level == 0) {
            writer.write("graphql`\n")
          }
          val (nonComment, comment) = splitComment(line)
          writer.write(escape(nonComment))
          writer.write(comment)
          writer.write("\n")
          val openSelectionSets = math.max(0, level - 1)
          val hadContent = openSelectionSets > 0 || !nonComment.isBlank
          val selectionSetDiff = countSelectionSetDiff(nonComment, hasComments = false)
          val nextOpenSelectionSets = openSelectionSets + selectionSetDiff
          if (nextOpenSelectionSets == 0 && hadContent) {
            writer.write("`\n")
            loop(0)
          } else {
            loop(1 + nextOpenSelectionSets)
          }
        case None if level > 0 =>
          throw new IllegalArgumentException("Encountered an unclosed selection set.")
        case None => // EOF
      }
    }
    loop(0)
  }

  private def escape(s: String): String =
    s.replace("""\""", """\\""").replace("`", """\`""").replaceAll("""\$(?=\{.*?})""", """\$""")

  def clean(cacheStoreFactory: CacheStoreFactory): Unit = {
    val Stores(last, resources, outputs) = Stores(cacheStoreFactory)
    last.delete()
    Tracked.diffInputs(resources, FileInfo.lastModified).clean()
    Tracked.diffOutputs(outputs, FileInfo.lastModified).clean()
  }
}
