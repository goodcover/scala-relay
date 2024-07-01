name := "basic"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"

enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)

scalacOptions += "-Ymacro-annotations"

scalaVersion := "2.13.14"

useYarn := true

//outputPath in Compile := (resourceDirectory in Compile).value / "testschema.graphql"

relayCompilerCommand := s"node ${(Compile / npmInstallDependencies).value}/node_modules/.bin/relay-compiler"

relaySchema := (Compile / resourceDirectory).value / "testschema.graphql"

relayDebug := true

Compile / npmDevDependencies ++= Seq(
  // TODO: This is temporary. We only want to produce JavaScript.
  // Version 14.1.0 uses relay >=10.1.3 and 14.1.1 uses >=12.0.0.
  "relay-compiler-language-typescript" -> "14.1.0",
  "typescript"                         -> "^4.2.4",
  "relay-compiler"                     -> "11.0.0",
  "graphql"                            -> "^15.4.0"
)

Compile / relayDisplayOnlyOnFailure := true

Compile / relayNpmDir := (Compile / npmInstallDependencies).value

webpack / version := "5.75.0"

logLevel := Level.Debug
