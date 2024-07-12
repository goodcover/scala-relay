name := "multi-project"

Global / scalaVersion := "2.13.14"

def commonSettings = Seq(
  scalacOptions += "-Ymacro-annotations",
  useYarn := true,
  relayCompilerCommand := s"node ${(Compile / npmInstallDependencies).value}/node_modules/.bin/relay-compiler",
  relaySchema := (LocalRootProject / baseDirectory).value / "testschema.graphql",
  relayDebug := true,
  Compile / npmDevDependencies ++= (Compile / relayDependencies).value,
  Compile / relayDisplayOnlyOnFailure := true,
  Compile / relayNpmDir := (Compile / npmInstallDependencies).value,
  webpack / version := "5.75.0",
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0"
)

lazy val root = project
  .in(file("."))
  .aggregate(a, b)

lazy val a = project
  .enablePlugins(ScalaRelayPlugin, ScalaJSBundlerPlugin)
  .settings(commonSettings)

lazy val b = project
  .enablePlugins(ScalaRelayPlugin, ScalaJSBundlerPlugin)
  .dependsOn(a)
  .settings(commonSettings)
  .settings(inConfig(Compile)(Seq(
    // This is for relayConvert.
    // We need to ensure that the converter knows about all fragments.
    relayGraphQLDependencies ++= (a / relayGraphQLFiles).value,
    // These are for relayCompile.
    // We should only include the graphql resources and extracts otherwise we compile twice.
    relayInclude ++= {
      val base = relayBaseDirectory.value.toPath
      (a / unmanagedResourceDirectories).value.map { dir =>
        base.relativize(dir.toPath).toString + "/**"
      }
    },
    relayInclude += relayBaseDirectory.value.toPath.relativize((a / relayExtractDirectory).value.toPath).toString + "/**",
    // Technically this only depends on relayExtract. We will need to run this at some point though.
    relayCompile := ((Compile / relayCompile) dependsOn (a / Compile / relayCompile)).value
  )))
  .settings(
    scalaJSUseMainModuleInitializer := true,
    webpackConfigFile := {
      val template = file("b") / "scalajs.webpack.config.js.template"
      val config = IO.read(template).replaceAllLiterally("${PWD}", baseDirectory.value.getAbsolutePath)
      val output = crossTarget.value / "scalajs.webpack.config.js"
      IO.write(output, config)
      Some(output)
    }
  )

logLevel := Level.Debug
