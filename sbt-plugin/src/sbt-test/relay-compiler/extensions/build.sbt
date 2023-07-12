name := "extensions"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"

enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)

scalacOptions += "-P:scalajs:sjsDefinedByDefault"

scalaVersion := "2.13.10"

useYarn := true

//outputPath in Compile := (resourceDirectory in Compile).value / "testschema.graphql"

relaySchema := (Compile / resourceDirectory).value / "testschema.graphql"

relayDebug := true

npmDevDependencies in Compile ++= Seq(
  "relay-compiler-language-scalajs" -> "0.25.13",
  "relay-compiler"                  -> "11.0.0",
  "graphql"                         -> "^15.4.0"
)

relayDisplayOnlyOnFailure in Compile := true

Compile / relayNpmDir := (Compile / npmInstallDependencies).value

webpack / version := "5.75.0"
