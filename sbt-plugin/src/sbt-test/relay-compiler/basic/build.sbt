name := "basic"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"

enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)

scalacOptions += "-Ymacro-annotations"

scalaVersion := "2.13.14"

useYarn := true

relayCompilerCommand := s"node ${(Compile / npmInstallDependencies).value}/node_modules/.bin/relay-compiler"

relaySchema := (Compile / resourceDirectory).value / "testschema.graphql"

relayDebug := true

Compile / npmDevDependencies ++= Seq( //
  "relay-compiler" -> "11.0.0",
  // TODO: Where is this required? Should it be part of the plugin?
  "graphql"        -> "^15.4.0"
)

Compile / relayDisplayOnlyOnFailure := true

Compile / relayNpmDir := (Compile / npmInstallDependencies).value

webpack / version := "5.75.0"

logLevel := Level.Debug
