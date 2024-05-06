name := "basicpersist"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"

enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)

scalacOptions += "-Ymacro-annotations"

scalaVersion := "2.13.13"

useYarn := true

relayCompilerCommand := s"node ${(Compile / npmInstallDependencies).value}/node_modules/.bin/relay-compiler"

relaySchema := (Compile / resourceDirectory).value / "testschema.graphql"

relayDebug := true

Compile / relayPersistedPath := Some((Compile / resourceDirectory).value / "persist.json")

Compile / npmDevDependencies ++= Seq(
  "relay-compiler-language-scalajs" -> s"link:${(Compile / npmUpdate / crossTarget).value.absolutePath}/node_modules/relay-compiler-language-scalajs",
  "relay-compiler"                  -> "11.0.0",
  "graphql"                         -> "^15.4.0"
)

Compile / relayDisplayOnlyOnFailure := true

Compile / relayNpmDir := (Compile / npmInstallDependencies).value

webpack / version := "5.75.0"
