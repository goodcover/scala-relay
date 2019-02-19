name := "basic"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"

enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)

scalacOptions += "-P:scalajs:sjsDefinedByDefault"

scalaVersion := "2.12.8"

useYarn := true

//outputPath in Compile := (resourceDirectory in Compile).value / "testschema.graphql"

relaySchema := (resourceDirectory in Compile).value / "testschema.graphql"

relayDebug := true

emitSourceMaps := false
