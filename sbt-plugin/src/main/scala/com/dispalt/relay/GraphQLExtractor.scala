package com.dispalt.relay

import sbt.*
import sbt.io.Using.fileWriter
import sbt.util.CacheImplicits.*
import sbt.util.{CacheStore, CacheStoreFactory}
import sjsonnew.*

import java.nio.charset.StandardCharsets
import scala.meta.*
import scala.meta.inputs.Input

object GraphQLExtractor {

  // Increment when the extraction code changes to bust the cache.
  private val Version = 1

  final case class Options(graphqlOutputDir: File, scalaOutputDir: File, typeScript: Boolean)

  object Options {
    //noinspection TypeAnnotation
    implicit val iso = LList.iso[Options, File :*: File :*: Boolean :*: LNil]( //
      { o: Options => //
        ("graphqlOutputDir" -> o.graphqlOutputDir) :*: ("scalaOutputDir" -> o.scalaOutputDir) :*: ("typeScript" -> o.typeScript) :*: LNil
      }, {
        case (_, graphqlOutputDir) :*: (_, scalaOutputDir) :*: (_, typeScript) :*: LNil => //
          Options(graphqlOutputDir, scalaOutputDir, typeScript)
      }
    )
  }

  final case class Results(graphqlSources: Set[File], scalaSources: Set[File])

  private final case class Extracts(graphql: File, scala: Set[File]) {
    def all: Set[File] = scala + graphql
  }

  private object Extracts {

    //noinspection TypeAnnotation
    implicit val iso = LList.iso[Extracts, File :*: Set[File] :*: LNil]( //
      { e: Extracts => //
        ("graphql" -> e.graphql) :*: ("scala" -> e.scala) :*: LNil
      }, {
        case (_, graphql) :*: (_, scala) :*: LNil => //
          Extracts(graphql, scala)
      }
    )
  }

  private type SourceExtracts = Map[File, Extracts]

  private final case class Analysis(version: Int, options: Options, extracts: SourceExtracts)

  private object Analysis {
    def apply(options: Options): Analysis = Analysis(Version, options, Map.empty)

    //noinspection TypeAnnotation
    implicit val iso = LList.iso[Analysis, Int :*: Options :*: SourceExtracts :*: LNil]( //
      { a: Analysis => //
        ("version" -> a.version) :*: ("options" -> a.options) :*: ("extracts" -> a.extracts) :*: LNil
      }, {
        case (_, version) :*: (_, options) :*: (_, extracts) :*: LNil => //
          Analysis(version, options, extracts)
      }
    )
  }

  private final case class Stores(last: CacheStore, sources: CacheStore, schema: CacheStore, outputs: CacheStore)

  private object Stores {
    def apply(cacheStoreFactory: CacheStoreFactory): Stores = Stores(
      last = cacheStoreFactory.make("last"),
      sources = cacheStoreFactory.make("sources"),
      schema = cacheStoreFactory.make("schema"),
      outputs = cacheStoreFactory.make("outputs")
    )
  }

  def extract(
    cacheStoreFactory: CacheStoreFactory,
    sources: Set[File],
    schemaFile: File,
    options: Options,
    logger: Logger
  ): Results = {
    logger.info("Extracting GraphQL...")
    val stores = Stores(cacheStoreFactory)
    val prevTracker = Tracked.lastOutput[Unit, Analysis](stores.last) { (_, maybePreviousAnalysis) =>
      val previousAnalysis = maybePreviousAnalysis.getOrElse(Analysis(options))
      logger.debug(s"Previous analysis:\n$previousAnalysis")
      // NOTE: Update clean if you change this.
      Tracked.diffInputs(stores.sources, FileInfo.lastModified)(sources) { sourcesReport =>
        logger.debug(s"Sources:\n$sourcesReport")
        // NOTE: Update clean if you change this.
        Tracked.diffInputs(stores.schema, FileInfo.lastModified)(Set(schemaFile)) { schemaReport =>
          logger.debug(s"Schema:\n$schemaReport")
          // There are 5 cases to handle:
          // 1) Version, schema, or options changed - delete all previous extracts and re-generate everything
          // 2) Source removed - delete the extract
          // 3) Source added - generate the extract
          // 4) Source modified - generate the extract
          // 5) Extract modified - generate the extract
          val schema = GraphQLSchema(schemaFile)
          val (modifiedExtracts, unmodifiedExtracts) =
            extractModified(sourcesReport, schemaReport, schema, previousAnalysis, options, logger)
          val modifiedOutputs   = modifiedExtracts.values.flatMap(_.all).toSet
          val unmodifiedOutputs = unmodifiedExtracts.values.flatMap(_.all).toSet
          val outputs           = modifiedOutputs ++ unmodifiedOutputs
          // NOTE: Update clean if you change this.
          Tracked.diffOutputs(stores.outputs, FileInfo.lastModified)(outputs) { outputsReport =>
            logger.debug(s"Outputs:\n$outputsReport")
            val unexpectedChanges = unmodifiedOutputs -- outputsReport.unmodified
            if (unexpectedChanges.nonEmpty) {
              val reversed = unmodifiedExtracts.flatMap {
                case (source, extracts) => extracts.all.map(_ -> source)
              }
              val needsExtraction = unexpectedChanges.flatMap(reversed.get)
              extractFiles(needsExtraction, schema, options, logger)
            }
          }
          val extracts = modifiedExtracts ++ unmodifiedExtracts
          Analysis(Version, options, extracts)
        }
      }
    }
    val extracts = prevTracker(()).extracts.values
    Results(extracts.map(_.graphql).toSet, extracts.flatMap(_.scala).toSet)
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
    schemaReport: ChangeReport[File],
    schema: GraphQLSchema,
    previousAnalysis: Analysis,
    options: Options,
    logger: Logger
  ): (SourceExtracts, SourceExtracts) = {
    def versionUnchanged = Version == previousAnalysis.version
    def schemaUnchanged  = schemaReport.modified.isEmpty
    def optionsUnchanged = options == previousAnalysis.options
    if (versionUnchanged && schemaUnchanged && optionsUnchanged) {
      logger.debug("Version, schema, and options have not changed")
      val outputsOfRemoved = previousAnalysis.extracts.filterKeys(sourceReport.removed.contains).values.flatMap(_.all)
      IO.delete(outputsOfRemoved)
      val addedOrChangedSources = sourceReport.modified -- sourceReport.removed
      val modifiedExtracts      = extractFiles(addedOrChangedSources, schema, options, logger)
      val unmodifiedExtracts    = previousAnalysis.extracts.filterKeys(sourceReport.unmodified.contains)
      (modifiedExtracts, unmodifiedExtracts)
    } else {
      def whatChanged =
        if (!versionUnchanged) "Version"
        else if (!schemaUnchanged) "Schema"
        else "Options"
      logger.debug(s"$whatChanged changed")
      val previousOutputs = previousAnalysis.extracts.values.flatMap(_.all)
      IO.delete(previousOutputs)
      val modifiedExtracts = extractFiles(sourceReport.checked, schema, options, logger)
      (modifiedExtracts, Map.empty)
    }
  }

  private def extractFiles(
    files: Iterable[File],
    schema: GraphQLSchema,
    options: Options,
    logger: Logger
  ): SourceExtracts =
    files.flatMap { file =>
      extractFile(file, schema, options, logger).map(file -> _)
    }.toMap

  private def extractFile(file: File, schema: GraphQLSchema, options: Options, logger: Logger): Option[Extracts] = {
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
      val scalaFiles  = writeScala(options, schema, definitions)
      val extracts    = Extracts(graphqlFile, scalaFiles)
      logger.debug(s"Extracted ${definitions.size} definitions to: $extracts")
      Some(extracts)
    } else {
      logger.debug("No definitions found")
      None
    }
  }

  private def writeGraphql(source: File, options: Options, definitions: Iterable[String]): File = {
    // relay-compiler doesn't seem to support loading executable definitions from .graphql files.
    // We have to write them to the same language file that we relay-compiler will output to.
    // See https://github.com/facebook/relay/issues/4726#issuecomment-2193708623.
    val extension   = if (options.typeScript) "ts" else "js"
    val graphqlFile = options.graphqlOutputDir / s"${source.base}.$extension"
    fileWriter(StandardCharsets.UTF_8)(graphqlFile) { writer =>
      definitions.foreach { definition =>
        writer.write("graphql`\n")
        writer.write(definition)
        writer.write("\n`\n")
      }
    }
    graphqlFile
  }

  private def writeScala(options: Options, schema: GraphQLSchema, definitions: Iterable[String]): Set[File] = {
    val writer = new ScalaWriter(options.graphqlOutputDir, schema)
    definitions.flatMap(writer.write).toSet
  }

  private def positionText(position: Position): String = {
    position.input match {
      case Input.File(path, _)        => s"${path.syntax}:${position.startLine}:${position.startColumn}"
      case Input.VirtualFile(path, _) => s"$path:${position.startLine}:${position.startColumn}"
      case _                          => position.toString
    }
  }

  def clean(cacheStoreFactory: CacheStoreFactory): Unit = {
    val Stores(last, sources, schema, outputs) = Stores(cacheStoreFactory)
    last.delete()
    Tracked.diffInputs(sources, FileInfo.lastModified).clean()
    Tracked.diffInputs(schema, FileInfo.lastModified).clean()
    Tracked.diffOutputs(outputs, FileInfo.lastModified).clean()
  }
}
