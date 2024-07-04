name := "basic"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"

enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)

scalacOptions += "-Ymacro-annotations"

scalaVersion := "2.13.14"

useYarn := true

relayCompilerCommand := s"node ${(Compile / npmInstallDependencies).value}/node_modules/.bin/relay-compiler"

relaySchema := (Compile / resourceDirectory).value / "graphql" / "Schema.graphqls"

relayDebug := true

Compile / npmDevDependencies ++= Seq( //
  "relay-compiler-language-typescript" -> relayTypeScriptVersion.value,
  "typescript"                         -> "^4.2.4",
  "relay-compiler"                     -> "11.0.0",
  // TODO: Where is this required? Should it be part of the plugin?
  "graphql" -> "^15.4.0"
)

Compile / relayDisplayOnlyOnFailure := true

Compile / relayNpmDir := (Compile / npmInstallDependencies).value

webpack / version := "5.75.0"

Compile / relayExclude += "generated/**"

logLevel := Level.Debug
