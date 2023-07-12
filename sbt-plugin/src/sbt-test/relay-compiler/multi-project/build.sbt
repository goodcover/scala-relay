name := "multi-project"

Global / scalaVersion := "2.13.10"

def commonSettings = Seq(
  scalacOptions += "-Ymacro-annotations",
  useYarn := true,
  relaySchema := (LocalRootProject / baseDirectory).value / "testschema.graphql",
  relayDebug := true,
  Compile / npmDevDependencies ++= Seq(
    "relay-compiler-language-scalajs"   -> s"link:${baseDirectory.value}/node_modules/relay-compiler-language-scalajs",
    "relay-compiler-language-scala2gql" -> s"link:${baseDirectory.value}/node_modules/relay-compiler-language-scala2gql",
    "relay-compiler"                    -> "11.0.0",
    "graphql"                           -> "^15.4.0"
  ),
  Compile / relayDisplayOnlyOnFailure := true,
  Compile / relayNpmDir := (Compile / npmInstallDependencies).value,
  webpack / version := "5.75.0"
)

lazy val a = project
  .enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)
  .settings(commonSettings)

lazy val b = project
  .enablePlugins(RelayGeneratePlugin, ScalaJSBundlerPlugin)
  .dependsOn(a)
  .settings(commonSettings)
