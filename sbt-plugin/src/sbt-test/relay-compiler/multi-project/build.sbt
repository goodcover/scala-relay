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
  webpack / version := "5.75.0"
)

lazy val a = project
  .enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)
  .settings(commonSettings)

lazy val b = project
  .enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)
  .dependsOn(a)
  .settings(commonSettings)
  .settings(inConfig(Compile)(relayInclude += (a / sourceDirectory).value))

logLevel := Level.Debug
