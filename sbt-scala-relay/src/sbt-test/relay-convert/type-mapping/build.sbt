name := "basic"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"

enablePlugins(ScalaRelayPlugin, ScalaJSBundlerPlugin)

scalacOptions += "-Ymacro-annotations"

scalaVersion := "2.13.16"

useYarn := true

relayCompilerCommand := s"node ${(Compile / npmInstallDependencies).value}/node_modules/.bin/relay-compiler"

relaySchema := (Compile / resourceDirectory).value / "graphql" / "Schema.graphqls"

relayDebug := true

Compile / npmDevDependencies ++= (Compile / relayDependencies).value

Compile / relayDisplayOnlyOnFailure := true

webpack / version := "5.75.0"

// Avoid picking up the expected files in the unmanagedSources.
unmanagedSources / excludeFilter ~= (_ || "*.scala")

Compile / relayConvertTypeMappings += "Foo" -> "Bar"

logLevel := Level.Debug
