name := "basic"

libraryDependencies += "org.scala-js"        %%% "scalajs-dom"        % "1.1.0"
libraryDependencies += "com.goodcover.relay" %%% "scala-relay-macros" % com.goodcover.relay.BuildInfo.version

enablePlugins(ScalaRelayPlugin, ScalaJSBundlerPlugin)

scalacOptions += "-Ymacro-annotations"

scalaVersion := "2.13.16"

useYarn := true

relayCompilerCommand := s"node ${(Compile / npmInstallDependencies).value}/node_modules/.bin/relay-compiler"

relaySchema := (Compile / resourceDirectory).value / "testschema.graphql"

relayDebug := true

Compile / npmDevDependencies ++= (Compile / relayDependencies).value

Compile / relayDisplayOnlyOnFailure := true

webpack / version := "5.75.0"

logLevel := Level.Debug
