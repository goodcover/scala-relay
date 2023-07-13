import sbtrelease.ReleaseCustom
import sbtrelease.ReleaseStateTransformations._

import java.nio.file.Files
import scala.sys.process._

// Run slinky-relay-ijext/updateIntellij
ThisBuild / updateIntellij := {}
ThisBuild / intellijBuild := "232.6095.10"

lazy val root =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(PgpKeys.publishSigned := {}, publishLocal := {}, publishArtifact := false, publish := {})
    .settings(
      // crossScalaVersions must be set to Nil on the aggregating project
      crossScalaVersions := Nil,
      publish / skip := true
    )
    .aggregate(`sbt-relay-compiler`, `relay-macro`)

def RuntimeLibPlugins = Sonatype
def SbtPluginPlugins  = Sonatype

lazy val `sbt-relay-compiler` = project
  .in(file("sbt-plugin"))
  .enablePlugins(SbtPluginPlugins)
  .enablePlugins(SbtPlugin)
  .settings(commonSettings ++ mavenSettings)
  .settings(
    sbtPlugin := true,
    addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % Version.Scalajs),
    addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.1"),
    scriptedLaunchOpts += "-Dplugin.version=" + version.value,
    scriptedBufferLog := false,
    scriptedDependencies := {
      scriptedDependencies.value

      (`relay-macro` / publishLocal).value

      val rootDir     = (LocalRootProject / baseDirectory).value
      val sourceDir   = "node-compiler"
      val packageName = "relay-compiler-language-scalajs"
      val cwd         = rootDir / sourceDir
      require(Process(Seq("yarn", "install"), cwd).! == 0)
      require(Process(Seq("yarn", "build"), cwd).! == 0)

      // Create a link in each test directory to the plugin so that it will get copied into the test.
      // Be careful of https://github.com/sbt/sbt/issues/7331.
      val testGroups = sbtTestDirectory.value.listFiles()
      testGroups.foreach { group =>
        val tests = group.listFiles()
        tests.foreach { test =>
          val nodeModulesDir = test / "node_modules"
          nodeModulesDir.mkdirs()
          val link = nodeModulesDir / packageName
          if (!link.exists()) {
            val target = rootDir / sourceDir
            Files.createSymbolicLink(link.toPath, target.toPath)
          }
        }
      }
    },
    scalaVersion := {
      (pluginCrossBuild / sbtBinaryVersion).value match {
        case "0.13" => "2.10.6"
        case _      => Version.Scala212
      }
    },
    crossScalaVersions := Nil,
    Compile / sourceGenerators += Def.task {
      Generators.version(version.value, (Compile / sourceManaged).value)
    }.taskValue
  )

lazy val `relay-macro` = project
  .in(file("relay-macro"))
  .enablePlugins(RuntimeLibPlugins && ScalaJSPlugin)
  .settings(commonSettings ++ mavenSettings)
  .settings(
    Compile / resourceGenerators += Def.task {
      val rootFolder = (Compile / resourceManaged).value / "META-INF"
      rootFolder.mkdirs()

      IO.write(
        rootFolder / "intellij-compat.json",
        s"""{"artifact": "${organization.value} % slinky-relay-ijext_2.13 % ${version.value}" }""".stripMargin
      )

      Seq(rootFolder / "intellij-compat.json")
    },
    scalacOptions ++= {
      if (scalaJSVersion.startsWith("0.6.")) Seq("-P:scalajs:sjsDefinedByDefault")
      else Nil
    },
    libraryDependencies ++= Seq(Library.scalatest, "org.scala-lang" % "scala-reflect" % scalaVersion.value),
    macroAnnotationSettings
  )

lazy val `slinky-relay-ijext` = (project in file("slinky-relay-ijext"))
  .enablePlugins(RuntimeLibPlugins && SbtIdeaPlugin)
  .settings(org.jetbrains.sbtidea.Keys.buildSettings)
  .settings(commonSettings ++ mavenSettings)
  .settings(
    crossScalaVersions := Seq(Version.Scala213),
    intellijPluginName := name.value,
    intellijPlugins ++= Seq("org.intellij.scala".toPlugin),
    intellijBuild := (ThisBuild / intellijBuild).value,
    intellijBaseDirectory := (ThisBuild / intellijBaseDirectory).value,
    packageMethod := PackagingMethod.Standalone(), // This only works for proper plugins
    patchPluginXml := pluginXmlOptions { xml =>
      // This only works for proper plugins
      xml.version = version.value
      xml.sinceBuild = (ThisBuild / intellijBuild).value
      xml.untilBuild = "240.*"
    },
    Compile / resourceGenerators += Def.task {
      val rootFolder = (Compile / resourceManaged).value / "META-INF"
      rootFolder.mkdirs()
      val fileOut = rootFolder / "intellij-compat.xml"

      IO.write(fileOut, s"""
          |<!DOCTYPE intellij-compat PUBLIC "Plugin/DTD"
          |        "https://raw.githubusercontent.com/JetBrains/intellij-scala/idea183.x/scala/scala-impl/src/org/jetbrains/plugins/scala/components/libextensions/intellij-compat.dtd">
          |<intellij-compat>
          |    <name>Slinky Relay Intellij Support</name>
          |    <description>Expands Slinky relay macros</description>
          |    <version>${version.value}</version>
          |    <vendor>Goodcover</vendor>
          |    <ideaVersion since-build="2020.3.0" until-build="2024.1.0">
          |        <extension interface="org.jetbrains.plugins.scala.lang.macros.evaluator.ScalaMacroTypeable"
          |             implementation="slinkyrelay.GraphQLGenInjector">
          |            <name>graphqlGen whitebox mac library Support</name>
          |            <description>Support for graphqlGen macro</description>
          |        </extension>
          |    </ideaVersion>
          |</intellij-compat>
          """.stripMargin)

      Seq(fileOut)
    }
  )

lazy val mavenSettings: Seq[Setting[_]] = Seq(publishMavenStyle := true, publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
})

lazy val macroAnnotationSettings = Seq(
  resolvers ++= Resolver.sonatypeOssRepos("releases"),
  scalacOptions ++= {
    if (scalaVersion.value == Version.Scala213) Seq("-Ymacro-annotations")
    else Seq("-Xfuture")
  },
  libraryDependencies ++= {
    if (scalaVersion.value == Version.Scala213) Seq.empty
    else Seq(compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full)))
  }
)

lazy val commonSettings: Seq[Setting[_]] = Seq(
  scalaVersion := Version.Scala213,
  crossScalaVersions := Seq(Version.Scala212, Version.Scala213),
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-unchecked",
    "-Xlint",
//    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-language:_",
    "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds",         // Allow higher-kinded types
    "-language:implicitConversions"  // Allow definition of implicit functions called views
  ),
  organization := "com.dispalt.relay",  // TODO - Coordinates
  sonatypeProfileName := "com.dispalt", // TODO - Coordinates
  pomExtra :=
    <developers>
        <developer>
          <id>dispalt</id>
          <name>Dan Di Spaltro</name>
          <url>http://dispalt.com</url>
        </developer>
        <developer>
          <id>kolarm</id>
          <name>Marko Kolar</name>
          <url>https://github.com/kolarm</url>
        </developer>
      </developers>,
  homepage := Some(url(s"https://github.com/goodcover/scala-relay")),
  licenses := Seq("MIT License" -> url("http://opensource.org/licenses/mit-license.php")),
  scmInfo := Some(
    ScmInfo(url("https://github.com/goodcover/scala-relay"), "scm:git:git@github.com:goodcover/scala-relay.git")
  )
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseCrossBuild := false

releaseProcess :=
  Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    releaseStepCommandAndRemaining("+ test"),
    releaseStepCommandAndRemaining(";+ slinky-relay-ijext/updateIntellij ;+ slinky-relay-ijext/test"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+ publishSigned"),
    releaseStepCommandAndRemaining("^ sbt-relay-compiler/publishSigned"),
    releaseStepCommandAndRemaining("+ slinky-relay-ijext/publishSigned"),
    releaseStepCommandAndRemaining("sonatypeReleaseAll"),
    ReleaseCustom.doReleaseYarn,
    setNextVersion,
    ReleaseCustom.commitNextVersion,
    pushChanges
  )

addCommandAlias("publishLocalAll", ";publishLocal ;slinky-relay-ijext/publishLocal")
