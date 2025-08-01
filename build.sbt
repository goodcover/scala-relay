import sbtrelease.ReleaseCustom
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

// Run scala-relay-ijext/updateIntellij
ThisBuild / updateIntellij := {}
ThisBuild / scalaVersion := Versions.Scala213
ThisBuild / organization := "com.goodcover.relay"

ThisBuild / intellijPluginName := "scala-relay-ijext"
// See https://www.jetbrains.com/intellij-repository/releases
// search for com.jetbrains.intellij.idea
ThisBuild / intellijBuild := "252"

ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions := Nil,
    publish / skip := true,
    PgpKeys.publishSigned := {},
    publishLocal := {},
    publishArtifact := false,
    publish := {}
  )
  .aggregate( //
    `sbt-scala-relay`,
    `scala-relay-core`,
    `scala-relay-macros`
  )

lazy val `sbt-scala-relay` = project
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(commonSettings)
  .settings(
    sbtPlugin := true,
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion),
    libraryDependencies ++= Seq(Dependencies.Caliban, Dependencies.ScalaMeta),
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "com.goodcover.relay",
    scriptedLaunchOpts += "-Dplugin.version=" + version.value,
    scriptedBufferLog := false,
    scriptedDependencies := {
      scriptedDependencies.value
      (`scala-relay-core` / publishLocal).value
      (`scala-relay-macros` / publishLocal).value
    },
    scalaVersion := Versions.Scala212,
    crossScalaVersions := Nil
  )

lazy val `scala-relay-core` = project
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)

lazy val `scala-relay-macros` = project
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies += Dependencies.ScalaReflect.value,
    Compile / resourceGenerators += Def.task {
      val rootFolder = (Compile / resourceManaged).value / "META-INF"
      rootFolder.mkdirs()
      val id = (`scala-relay-ijext` / projectID).value
      IO.write(
        rootFolder / "intellij-compat.json",
        s"""{"artifact": "${id.organization} % ${id.name}_2.13 % ${id.revision}" }""".stripMargin
      )
      Seq(rootFolder / "intellij-compat.json")
    },
    macroAnnotationSettings
  )

lazy val `scala-relay-ijext` = project
  .enablePlugins(SbtIdeaPlugin)
  .settings(org.jetbrains.sbtidea.Keys.buildSettings)
  .settings(commonSettings)
  .settings(
    crossScalaVersions := Seq(Versions.Scala213),
    intellijPluginName := name.value,
    intellijPlugins ++= Seq("org.intellij.scala".toPlugin),
    intellijBuild := (ThisBuild / intellijBuild).value,
    intellijBaseDirectory := (ThisBuild / intellijBaseDirectory).value,
    packageMethod := PackagingMethod.Standalone(), // This only works for proper plugins
    patchPluginXml := pluginXmlOptions { xml =>
      xml.version = version.value
      xml.sinceBuild = (ThisBuild / intellijBuild).value
    },
    Compile / resourceGenerators += Def.task {
      val rootFolder = (Compile / resourceManaged).value / "META-INF"
      rootFolder.mkdirs()
      val fileOut = rootFolder / "intellij-compat.xml"

      IO.write(fileOut, s"""
          |<!DOCTYPE intellij-compat PUBLIC "Plugin/DTD"
          |        "https://raw.githubusercontent.com/JetBrains/intellij-scala/idea183.x/scala/scala-impl/src/org/jetbrains/plugins/scala/components/libextensions/intellij-compat.dtd">
          |<intellij-compat>
          |    <id>com.goodcover.relay</id>
          |    <name>Scala Relay Intellij Support</name>
          |    <description>Expands scala-relay macros</description>
          |    <version>${version.value}</version>
          |    <vendor>Goodcover</vendor>
          |    <ideaVersion since-build="2020.3.0">
          |        <extension interface="org.jetbrains.plugins.scala.lang.macros.evaluator.ScalaMacroTypeable"
          |             implementation="com.goodcover.relay.GraphQLGenInjector">
          |            <name>graphqlGen whitebox mac library Support</name>
          |            <description>Support for graphqlGen macro</description>
          |        </extension>
          |    </ideaVersion>
          |</intellij-compat>
          """.stripMargin)

      Seq(fileOut)
    }
  )

lazy val macroAnnotationSettings = Seq(
  scalacOptions ++= {
    if (scalaVersion.value == Versions.Scala213) Seq("-Ymacro-annotations")
    else Seq("-Xfuture")
  },
  libraryDependencies ++= {
    if (scalaVersion.value == Versions.Scala213) Seq.empty
    else Seq(compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full)))
  }
)

lazy val commonSettings: Seq[Setting[_]] = Seq(
  scalaVersion := Versions.Scala213,
  crossScalaVersions := Seq(Versions.Scala213),
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-unchecked",
    "-Xlint",
//    "-Yno-adapted-args",
    "-release",
    "11",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-language:_",
    "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds",         // Allow higher-kinded types
    "-language:implicitConversions"  // Allow definition of implicit functions called views
  ),
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
      <developer>
        <id>steinybot</id>
        <name>Jason Pickens</name>
        <url>https://github.com/steinybot</url>
      </developer>
    </developers>,
  homepage := Some(url(s"https://github.com/goodcover/scala-relay")),
  licenses := Seq("MIT License" -> url("http://opensource.org/licenses/mit-license.php")),
  scmInfo := Some(
    ScmInfo(url("https://github.com/goodcover/scala-relay"), "scm:git:git@github.com:goodcover/scala-relay.git")
  ),
  publishMavenStyle := true
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseCrossBuild := false

releaseProcess :=
  Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    ReleaseCustom.checkAuthedGh,
    runClean,
    releaseStepCommandAndRemaining("+ test"),
    releaseStepCommandAndRemaining(";+ scala-relay-ijext/updateIntellij ;+ scala-relay-ijext/test"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+ publishSigned"),
    releaseStepCommandAndRemaining("^ sbt-scala-relay/publishSigned"),
    releaseStepCommandAndRemaining("+ scala-relay-ijext/publishSigned"),
    releaseStepCommand("sonaUpload"),
    setNextVersion,
    ReleaseCustom.commitNextVersion,
    pushChanges,
    ReleaseCustom.createGhRelease
  )

addCommandAlias("publishLocalAll", ";publishLocal ;scala-relay-ijext/publishLocal")
