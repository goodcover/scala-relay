name := "extensions"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"

enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)

scalacOptions += "-P:scalajs:sjsDefinedByDefault"

scalaVersion := "2.13.14"

useYarn := true

//outputPath in Compile := (resourceDirectory in Compile).value / "testschema.graphql"

relaySchema := (Compile / resourceDirectory).value / "testschema.graphql"

relayDebug := true

Compile / npmDevDependencies ++= Seq(
  "relay-compiler-language-scalajs" -> s"link:${baseDirectory.value}/node_modules/relay-compiler-language-scalajs",
  "relay-compiler"                  -> "11.0.0",
  "graphql"                         -> "^15.4.0"
)

Compile / relayDisplayOnlyOnFailure := true

Compile / relayNpmDir := (Compile / npmInstallDependencies).value

webpack / version := "5.75.0"
