package com.dispalt.relay

import sbt.{AutoPlugin, SettingKey}
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

import sbt.Keys._
import sbt._

import scalajsbundler.util.Commands

object RelayBasePlugin extends AutoPlugin {

  override def requires = ScalaJSPlugin

  override def trigger = noTrigger

  object autoImport {
    val relaySchema: SettingKey[File]           = settingKey[File]("Path to schema file")
    val relayValidateQuery: SettingKey[Boolean] = settingKey[Boolean]("Validate queries in macro")
    val relaySangriaVersion: SettingKey[String] = settingKey[String]("Set the Sangria version")
    val relayUseNulls: SettingKey[Boolean] =
      settingKey[Boolean]("Change the generated output to use nulls and not naked types")
    val relayDebug: SettingKey[Boolean] = settingKey[Boolean]("Debug the relay compiler")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
        /**
          * Set the version of the sangria compiler which helps validate GQL
          */
        relaySangriaVersion := "1.4.0",
        /**
          * Runtime dependency on the macro
          */
        libraryDependencies ++= Seq("com.dispalt.relay"   %%% "relay-macro" % com.dispalt.relay.core.SRCVersion.current,
                                    "org.sangria-graphql" %% "sangria"      % relaySangriaVersion.value % Provided),
        /**
          * Should we validate queries?
          */
        relayValidateQuery := false,
        /**
          * Should we codegen optional fields as A | Null (if true) or just A (if false).
          */
        relayUseNulls := false,
        /**
          * Set this if you'd like to see timing and larger stack traces.
          */
        relayDebug := false)
}

object RelayFilePlugin extends AutoPlugin {

  override def requires =
    ScalaJSPlugin && RelayBasePlugin

  override def trigger = noTrigger

  object autoImport {

    val relayCompile: TaskKey[Seq[File]] = taskKey[Seq[File]]("Run the relay compiler")
    val relayOutput: SettingKey[File]    = settingKey[File]("Output of the schema stuff")
    val relayCompilerPath: SettingKey[String] =
      settingKey[String]("The location of the `scala-relay-compiler` executable.")
    val relayBaseDirectory: SettingKey[File]      = settingKey[File]("The base directory the relay compiler")
    val relayExtraIncludes: SettingKey[Seq[File]] = settingKey[Seq[File]]("extra directories to include")

  }

  import RelayBasePlugin.autoImport._
  import autoImport._

  val relayFolder = "relay-compiler-out"

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
        /**
          * Add the path so the macro can find the schema.
          */
        scalacOptions += s"-Xmacro-settings:relaySchema=${relaySchema.value.absolutePath}",
        /**
          * If set to validate, then pass the setting again to the macro.
          */
        scalacOptions ++= (if (relayValidateQuery.value) Seq(s"-Xmacro-settings:relayValidateQuery=true") else Seq()),
        /**
          * Output path of the relay compiler.  Necessary this is an empty directory as it will
          * delete files it thinks went away.
          */
        relayOutput in Compile := (sourceManaged in Compile).value / relayFolder,
        /**
          *
          */
        relayCompilerPath := {
          "scala-relay-compiler"
        },
        /**
          * So this should normally default to the base directory, but in some cases if you want to include stuff
          * outside the directory, changing this should be considered.
          */
        relayBaseDirectory := baseDirectory.value,
        relayExtraIncludes := Seq((sourceDirectory in Compile).value),
        /**
          * Meat of the function.
          */
        relayCompile in Compile := Def
          .task[Seq[File]] {
            import Path.{flat, relativeTo}
            val cache         = streams.value.cacheDirectory / "relay-compile"
            val sourceFiles   = (unmanagedSourceDirectories in Compile).value
            val resourceFiles = (resourceDirectories in Compile).value
            val outpath       = (relayOutput in Compile).value
            val compilerPath  = relayCompilerPath.value
            val verbose       = relayDebug.value
            val useNulls      = relayUseNulls.value
            val schemaPath    = relaySchema.value
            val source        = relayBaseDirectory.value
            val extras        = relayExtraIncludes.value.pair(relativeTo(source)).map(_._2).toList

            IO.createDirectory(outpath)

            // Filter based on the presence of the annotation. and look for a change
            // in the schema path
            val scalaFiles =
              (sourceFiles ** "*.scala").get
                .filter(IO.read(_).contains("@gql"))
                .toSet ++ Set(schemaPath) ++ (resourceFiles ** "*.gql").get.toSet
            val label      = Reference.display(thisProjectRef.value)
            val workingDir = file(sys.props("user.dir"))
            val logger     = streams.value.log

            sbt.shim.SbtCompat.FileFunction
              .cached(cache)(FilesInfo.hash, FilesInfo.exists)(handleUpdate(label = label,
                                                                            workingDir = workingDir,
                                                                            compilerPath = compilerPath,
                                                                            schemaPath = schemaPath,
                                                                            sourceDirectory = source,
                                                                            outputPath = outpath,
                                                                            logger = logger,
                                                                            verbose = verbose,
                                                                            useNulls = useNulls,
                                                                            extras = extras))(scalaFiles)

            outpath.listFiles()
          }
          .value,
        /**
          * Hook the relay compiler into the compile pipeline.
          */
        sourceGenerators in Compile += (relayCompile in Compile).taskValue)

  implicit class QuoteStr(s: String) {
    def quote: String = "\"" + s + "\""
  }

  def runCompiler(workingDir: File,
                  compilerPath: String,
                  schemaPath: File,
                  sourceDirectory: File,
                  outputPath: File,
                  logger: Logger,
                  verbose: Boolean,
                  useNulls: Boolean,
                  extras: List[String]): Unit = {

    // TODO: this sucks not sure how to get npm scripts to work from java PB.
    val shell = if (System.getProperty("os.name").toLowerCase().contains("win")) {
      List("cmd.exe", "/C")
    } else List("sh", "-c")

    val verboseList  = if (verbose) "--verbose" :: Nil else Nil
    val useNullsList = if (useNulls) "--useNulls" :: Nil else Nil
    val extrasList   = extras flatMap (e => "--extra" :: e.quote :: Nil)

    val cmd = shell :+ (List(compilerPath,
                             "--schema",
                             schemaPath.getAbsolutePath.quote,
                             "--src",
                             sourceDirectory.getAbsolutePath.quote,
                             "--out",
                             outputPath.getAbsolutePath.quote) ::: verboseList ::: useNullsList ::: extrasList)
      .mkString(" ")

    println(cmd)
    Commands.run(cmd, workingDir, logger)
  }

  def handleUpdate(label: String,
                   workingDir: File,
                   compilerPath: String,
                   schemaPath: File,
                   sourceDirectory: File,
                   outputPath: File,
                   logger: Logger,
                   verbose: Boolean,
                   useNulls: Boolean,
                   extras: List[String])(in: ChangeReport[File], out: ChangeReport[File]): Set[File] = {
    val files = in.modified -- in.removed
    sbt.shim.SbtCompat.Analysis
      .counted("Scala source", "", "s", files.size)
      .foreach { count =>
        logger.info(s"Executing relayCompile on $count $label...")
        runCompiler(workingDir = workingDir,
                    compilerPath = compilerPath,
                    schemaPath = schemaPath,
                    sourceDirectory = sourceDirectory,
                    outputPath = outputPath,
                    logger = logger,
                    verbose = verbose,
                    useNulls = useNulls,
                    extras = extras)
        logger.info(s"Finished relayCompile.")
      }
    files
  }
}
