name := "basic"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"

enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)

scalacOptions += "-P:scalajs:sjsDefinedByDefault"

scalaVersion := "2.12.8"

useYarn := true

//outputPath in Compile := (resourceDirectory in Compile).value / "testschema.graphql"

relaySchema := (resourceDirectory in Compile).value / "testschema.graphql"

relayDebug := true

Compile / relayPersistedPath := Some((resourceDirectory in Compile).value / "persist.json")

emitSourceMaps := false

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

npmDevDependencies in Compile ++= Seq(
  "relay-compiler-language-scalajs" -> "0.22.2",
  "relay-compiler"                  -> "6.0.0",
  "graphql"                         -> "^14.1.0"
)

relayDisplayOnlyOnFailure in Compile := true
