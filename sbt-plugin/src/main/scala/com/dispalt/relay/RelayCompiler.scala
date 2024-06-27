package com.dispalt.relay

import sbt.*

import java.io.InputStream

object RelayCompiler {

  private implicit class QuoteStr(val s: String) extends AnyVal {
    def quote: String = "\"" + s + "\""
  }

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

  def compile(
    workingDir: File,
    compilerCommand: String,
    schemaPath: File,
    sourceDirectory: File,
    outputPath: File,
    logger: Logger,
    verbose: Boolean,
    extras: List[String],
    persisted: Option[File],
    customScalars: Map[String, String],
    displayOnFailure: Boolean
  ) = {
    run(
      workingDir = workingDir,
      compilerCommand = compilerCommand,
      schemaPath = schemaPath,
      sourceDirectory = sourceDirectory,
      outputPath = outputPath,
      logger = logger,
      verbose = verbose,
      extras = extras,
      persisted = persisted,
      customScalars = customScalars,
      displayOnFailure = displayOnFailure
    )
  }

  private def run(
    workingDir: File,
    compilerCommand: String,
    schemaPath: File,
    sourceDirectory: File,
    outputPath: File,
    logger: Logger,
    verbose: Boolean,
    extras: List[String],
    persisted: Option[File],
    customScalars: Map[String, String],
    displayOnFailure: Boolean
  ): Unit = {
    // TODO: this sucks not sure how to get npm scripts to work from java PB.
    val shell = if (System.getProperty("os.name").toLowerCase().contains("win")) {
      List("cmd.exe", "/C")
    } else List("sh", "-c")

    val verboseList = if (verbose) "--verbose" :: Nil else Nil
    val extrasList  = extras flatMap (e => "--include" :: e.quote :: Nil)
    val persistedList = persisted match {
      case Some(value) => List("--persist-output", value.getPath.quote)
      case None        => Nil
    }

    val customScalarsArgs = customScalars.map {
      case (scalarType, scalaType) => s"--customScalars.${scalarType}=${scalaType}"
    }.toList

    val cmd = shell :+ (List(
      compilerCommand,
      "--language",
      "typescript",
      "--watchman",
      "false",
      "--schema",
      schemaPath.getAbsolutePath.quote,
      "--src",
      sourceDirectory.getAbsolutePath.quote,
      "--artifactDirectory",
      outputPath.getAbsolutePath.quote
    ) ::: verboseList ::: extrasList ::: persistedList ::: customScalarsArgs)
      .mkString(" ")

    var output = Vector.empty[String]

    Commands.run(
      cmd,
      workingDir,
      logger,
      (is: InputStream) => output = scala.io.Source.fromInputStream(is).getLines.toVector
    ) match {
      case Left(value) =>
        output.foreach(logger.error(_))
        sys.error(s"Relay compiler failed, ${value}")

      case Right(_) =>
        if (!displayOnFailure) {
          output.foreach(logger.info(_))
        }
    }
  }

  def clean(): Unit = ???
}
