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
  .enablePlugins(RelayPlugin, ScalaJSBundlerPlugin)
  .settings(commonSettings)

lazy val b = project
  .enablePlugins(RelayPlugin, ScalaJSBundlerPlugin)
  .dependsOn(a)
  .settings(commonSettings)
  .settings(inConfig(Compile)(Seq(
    relayInclude += relayBaseDirectory.value.toPath.relativize((a / relayWrapDirectory).value.toPath).toString + "/**",
    relayCompile := ((Compile / relayCompile) dependsOn (a / Compile / relayWrap)).value
  )))

logLevel := Level.Debug
