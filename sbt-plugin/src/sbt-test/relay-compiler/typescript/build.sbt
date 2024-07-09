name := "basic"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"

enablePlugins(RelayPlugin, ScalaJSBundlerPlugin)

scalacOptions += "-Ymacro-annotations"

scalaVersion := "2.13.14"

useYarn := true

relayCompilerCommand := s"node ${(Compile / npmInstallDependencies).value}/node_modules/.bin/relay-compiler"

relaySchema := (Compile / resourceDirectory).value / "graphql" / "Schema.graphqls"

relayDebug := true

Compile / npmDevDependencies ++= (Compile / relayDependencies).value

Compile / relayDisplayOnlyOnFailure := true

Compile / relayNpmDir := (Compile / npmInstallDependencies).value

webpack / version := "5.75.0"

Compile / relayTypeScript := true

logLevel := Level.Debug
