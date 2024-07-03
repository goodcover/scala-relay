package com.dispalt.relay

import sbt.*
import sbt.nio.file.FileTreeView
import sbt.util.CacheImplicits.*
import sbt.util.{CacheStore, CacheStoreFactory}
import sjsonnew.*

import java.io.InputStream

object RelayCompiler {

  // Increment when the code changes to bust the cache.
  private val Version = 1

  private implicit class QuoteStr(val s: String) extends AnyVal {
    def quote: String = {
      require(!s.contains('\''))
      "'" + s + "'"
    }
  }

  final case class Options(
    workingDir: File,
    compilerCommand: String,
    schemaPath: File,
    sourceDirectory: File,
    outputPath: File,
    verbose: Boolean,
    includes: Seq[String],
    excludes: Seq[String],
    persisted: Option[File],
    customScalars: Map[String, String],
    displayOnFailure: Boolean,
    typeScript: Boolean
  ) {
    def language: String = if (typeScript) "typescript" else "javascript"
  }

  object Options {

    //noinspection TypeAnnotation
    implicit val iso = LList
      .iso[Options, File :*: String :*: File :*: File :*: File :*: Boolean :*: Seq[String] :*: Seq[String] :*: Option[
        File
      ] :*: Map[String, String] :*: Boolean :*: Boolean :*: LNil]( //
        { o: Options =>                                            //
          ("workingDir"         -> o.workingDir) :*:
            ("compilerCommand"  -> o.compilerCommand) :*:
            ("schemaPath"       -> o.schemaPath) :*:
            ("sourceDirectory"  -> o.sourceDirectory) :*:
            ("outputPath"       -> o.outputPath) :*:
            ("verbose"          -> o.verbose) :*:
            ("includes"         -> o.includes) :*:
            ("excludes"         -> o.excludes) :*:
            ("persisted"        -> o.persisted) :*:
            ("customScalars"    -> o.customScalars) :*:
            ("displayOnFailure" -> o.displayOnFailure) :*:
            ("typeScript"       -> o.typeScript) :*:
            LNil
        }, {
          case (_, workingDir) :*:
                (_, compilerCommand) :*:
                (_, schemaPath) :*:
                (_, sourceDirectory) :*:
                (_, outputPath) :*:
                (_, verbose) :*:
                (_, includes) :*:
                (_, excludes) :*:
                (_, persisted) :*:
                (_, customScalars) :*:
                (_, displayOnFailure) :*:
                (_, typeScript) :*:
                LNil => //
            Options(
              workingDir,
              compilerCommand,
              schemaPath,
              sourceDirectory,
              outputPath,
              verbose,
              includes,
              excludes,
              persisted,
              customScalars,
              displayOnFailure,
              typeScript
            )
        }
      )
  }

  type Results = Set[File]

  private type Artifacts = Results

  private final case class Analysis(version: Int, options: Options, artifacts: Artifacts)

  private object Analysis {
    def apply(options: Options): Analysis = Analysis(Version, options, Set.empty)

    //noinspection TypeAnnotation
    implicit val iso = LList.iso[Analysis, Int :*: Options :*: Artifacts :*: LNil]( //
      { a: Analysis => //
        ("version" -> a.version) :*: ("options" -> a.options) :*: ("artifacts" -> a.artifacts) :*: LNil
      }, {
        case (_, version) :*: (_, options) :*: (_, artifacts) :*: LNil => //
          Analysis(version, options, artifacts)
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

  def compile(cacheStoreFactory: CacheStoreFactory, options: Options, logger: Logger): Results = {
    val stores = Stores(cacheStoreFactory)
    val prevTracker = Tracked.lastOutput[Unit, Analysis](stores.last) { (_, maybePreviousAnalysis) =>
      val previousAnalysis = maybePreviousAnalysis.getOrElse(Analysis(options))
      logger.debug(s"Previous analysis:\n$previousAnalysis")
      val sources = findSources(options)
      // NOTE: Update clean if you change this.
      Tracked.diffInputs(stores.sources, FileInfo.lastModified)(sources) { sourcesReport =>
        logger.debug(s"Sources:\n$sourcesReport")
        // NOTE: Update clean if you change this.
        val artifacts = Tracked.diffOutputs(stores.outputs, FileInfo.lastModified) { outputsReport =>
          logger.debug(s"Previous outputs:\n$outputsReport")
          // If anything changes we need to delete all artifacts and recompile everything.
          // We could potentially run relay-compile in watch mode if necessary.
          if (Version != previousAnalysis.version ||
              options != previousAnalysis.options ||
              sourcesReport.modified.nonEmpty ||
              outputsReport.modified.nonEmpty) {
            if (outputsReport.modified.nonEmpty) {
              logger.warn("Unexpected modifications found to files:")
              outputsReport.modified.foreach { file =>
                logger.warn(s" ${file.absolutePath}")
              }
              logger.warn("Ensure that nothing is modifying these files so as to get the most benefit from the cache.")
            }
            IO.delete(previousAnalysis.artifacts)
            run(options, logger)
            findArtifacts(options)
          } else {
            previousAnalysis.artifacts
          }
        }
        Analysis(Version, options, artifacts)
      }
    }
    prevTracker(()).artifacts
  }

  private def findSources(options: Options): Set[File] = {
    val includes = absoluteGlobs(options.includes, options.sourceDirectory)
    val excludes = absoluteGlobs(options.excludes, options.sourceDirectory)
    val files    = FileTreeView.default.list(includes, !PathFilter(excludes: _*)).map(_._1.toFile).toSet
    files
  }

  private def absoluteGlobs(globs: Seq[String], baseDir: File): Seq[Glob] =
    globs.map { glob =>
      Glob(glob) match {
        case relative: RelativeGlob => Glob(baseDir, relative)
        case nonRelative            => nonRelative
      }
    }

  private def findArtifacts(options: Options): Set[File] = {
    val finder = options.outputPath ** (if (options.typeScript) "*.ts" else "*.js")
    finder.get().toSet
  }

  private def run(options: Options, logger: Logger): Unit = {
    import options.*

    // Version 11 Help:
    //
    // Create Relay generated files
    //
    // relay-compiler --schema <path> --src <path> [--watch]
    //
    // Options:
    //   --version             Show version number                            [boolean]
    //   --schema              Path to schema.graphql or schema.json[string] [required]
    //   --src                 Root directory of application code   [string] [required]
    //   --include             Directories to include under src
    //                                                        [array] [default: ["**"]]
    //   --exclude             Directories to ignore under src        [array] [default:
    //                  ["**/node_modules/**","**/__mocks__/**","**/__generated__/**"]]
    //   --extensions          File extensions to compile (defaults to extensions
    //                         provided by the language plugin)                 [array]
    //   --verbose             More verbose logging          [boolean] [default: false]
    //   --quiet               No output to stdout           [boolean] [default: false]
    //   --watchman            Use watchman when not in watch mode
    //                                                        [boolean] [default: true]
    //   --watch               If specified, watches files and regenerates on changes
    //                                                       [boolean] [default: false]
    //   --validate            Looks for pending changes and exits with non-zero code
    //                         instead of writing to disk    [boolean] [default: false]
    //   --persistFunction     An async function (or path to a module exporting this
    //                         function) which will persist the query text and return
    //                         the id.                                         [string]
    //   --persistOutput       A path to a .json file where persisted query metadata
    //                         should be saved. Will use the default implementation
    //                         (md5 hash) if `persistFunction` is not passed.  [string]
    //   --repersist           Run the persist function even if the query has not
    //                         changed.                      [boolean] [default: false]
    //   --noFutureProofEnums  This option controls whether or not a catch-all entry is
    //                         added to enum type definitions for values that may be
    //                         added in the future. Enabling this means you will have
    //                         to update your application whenever the GraphQL server
    //                         schema adds new enum values to prevent it from breaking.
    //                                                       [boolean] [default: false]
    //   --language            The name of the language plugin used for input files and
    //                         artifacts               [string] [default: "javascript"]
    //   --artifactDirectory   A specific directory to output all artifacts to. When
    //                         enabling this the babel plugin needs `artifactDirectory`
    //                         set as well.                                    [string]
    //   --customScalars       Mappings from custom scalars in your schema to built-in
    //                         GraphQL types, for type emission purposes. (Uses yargs
    //                         dot-notation, e.g. --customScalars.URL=String)
    //   --eagerESModules      This option enables emitting es modules artifacts.
    //                                                       [boolean] [default: false]
    //   --help                Show help                                      [boolean]

    // TODO: this sucks not sure how to get npm scripts to work from java PB.
    val shell = if (System.getProperty("os.name").toLowerCase().contains("win")) {
      Seq("cmd.exe", "/C")
    } else Seq("sh", "-c")

    val argsList = Seq(
      compilerCommand,
      "--language",
      language,
      "--watchman",
      "false",
      "--schema",
      schemaPath.getAbsolutePath.quote,
      "--src",
      sourceDirectory.getAbsolutePath.quote,
      "--artifactDirectory",
      outputPath.getAbsolutePath.quote
    )

    val verboseList = if (verbose) Seq("--verbose") else Seq.empty

    val includesList = includes flatMap (include => Seq("--include", include.quote))

    val excludesList = excludes flatMap (exclude => Seq("--exclude", exclude.quote))

    val persistedList = persisted match {
      case Some(value) => Seq("--persist-output", value.getPath.quote)
      case None        => Seq.empty
    }

    val customScalarsArgs = customScalars.map {
      case (scalarType, scalaType) => s"--customScalars.$scalarType=$scalaType"
    }.toSeq

    val cmd = shell :+ (argsList ++ verboseList ++ includesList ++ excludesList ++ persistedList ++ customScalarsArgs)
      .mkString(" ")

    var output = Vector.empty[String]

    logger.info("Running relay-compiler...")

    Commands.run(
      cmd,
      workingDir,
      logger,
      (is: InputStream) => output = scala.io.Source.fromInputStream(is).getLines.toVector
    ) match {
      case Left(value) =>
        output.foreach(logger.error(_))
        sys.error(s"Relay compiler failed, $value")

      case Right(_) =>
        if (!displayOnFailure) {
          output.foreach(logger.info(_))
        }
    }
  }

  def clean(cacheStoreFactory: CacheStoreFactory): Unit = {
    val Stores(last, sources, outputs) = Stores(cacheStoreFactory)
    last.delete()
    Tracked.diffInputs(sources, FileInfo.lastModified).clean()
    Tracked.diffOutputs(outputs, FileInfo.lastModified).clean()
  }
}
