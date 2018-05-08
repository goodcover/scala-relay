name := "basic"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"

enablePlugins(RelayFilePlugin, ScalaJSBundlerPlugin)

scalacOptions += "-P:scalajs:sjsDefinedByDefault"

scalaVersion := "2.12.4"

useYarn := true

//outputPath in Compile := (resourceDirectory in Compile).value / "testschema.graphql"

relaySchema := (resourceDirectory in Compile).value / "testschema.graphql"

relayDebug := true

relayValidateQuery := false

emitSourceMaps := false

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

// temporary workaround for https://github.com/scalameta/paradise/issues/10
scalacOptions in (Compile, console) := Seq() // macroparadise plugin doesn't work in repl yet.
// temporary workaround for https://github.com/scalameta/paradise/issues/55
sources in (Compile, doc) := Nil // macroparadise doesn't work with scaladoc yet.
