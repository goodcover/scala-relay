name := "basic"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"

enablePlugins(RelayPlugin)

enablePlugins(ScalaJSBundlerPlugin)

scalaVersion := "2.11.11"

useYarn := true

//outputPath in Compile := (resourceDirectory in Compile).value / "testschema.graphql"

schemaPath := (resourceDirectory in Compile).value / "testschema.graphql"

// A dependency on macro paradise 3.x is required to both write and expand
// new-style macros.  This is similar to how it works for old-style macro
// annotations and a dependency on macro paradise 2.x.
addCompilerPlugin(
  "org.scalameta" % "paradise" % "3.0.0-M8" cross CrossVersion.full)
scalacOptions += "-Xplugin-require:macroparadise"
libraryDependencies += "org.scalameta" %% "scalameta" % "1.7.0"
// temporary workaround for https://github.com/scalameta/paradise/issues/10
scalacOptions in (Compile, console) := Seq() // macroparadise plugin doesn't work in repl yet.
// temporary workaround for https://github.com/scalameta/paradise/issues/55
sources in (Compile, doc) := Nil // macroparadise doesn't work with scaladoc yet.
