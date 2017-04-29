

name := "basic"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"

enablePlugins(RelayPlugin)

enablePlugins(ScalaJSBundlerPlugin)

useYarn := true

//outputPath in Compile := (resourceDirectory in Compile).value / "testschema.graphql"

schemaPath := (resourceDirectory in Compile).value / "testschema.graphql"
