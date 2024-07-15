name := "extensions"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"

enablePlugins(ScalaRelayPlugin, ScalaJSBundlerPlugin)

scalacOptions += "-P:scalajs:sjsDefinedByDefault"

scalaVersion := "2.13.14"

useYarn := true

//outputPath in Compile := (resourceDirectory in Compile).value / "testschema.graphql"

relayCompilerCommand := s"node ${(Compile / npmInstallDependencies).value}/node_modules/.bin/relay-compiler"

relaySchema := (Compile / resourceDirectory).value / "testschema.graphql"

relayDebug := true

Compile / npmDevDependencies ++= (Compile / relayDependencies).value

Compile / relayDisplayOnlyOnFailure := true

webpack / version := "5.75.0"

logLevel := Level.Debug
