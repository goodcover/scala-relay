import sbtrelease.ReleaseCustom
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

// Run scala-relay-ijext/updateIntellij
ThisBuild / updateIntellij := {}
ThisBuild / intellijBuild := "241.17890.1"

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
  .enablePlugins(SbtPlugin, BuildInfoPlugin, Sonatype)
  .settings(commonSettings ++ mavenSettings)
  .settings(
    sbtPlugin := true,
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
  .enablePlugins(ScalaJSPlugin, Sonatype)
  .settings(commonSettings ++ mavenSettings)

lazy val `scala-relay-macros` = project
  .enablePlugins(ScalaJSPlugin, Sonatype)
  .settings(commonSettings ++ mavenSettings)
  .settings(
    libraryDependencies += Dependencies.ScalaReflect.value,
    Compile / resourceGenerators += Def.task {
      val rootFolder = (Compile / resourceManaged).value / "META-INF"
      rootFolder.mkdirs()
      val id = (`scala-relay-ijext` / projectID).value
      IO.write(
        rootFolder / "intellij-compat.json",
        s"""{"artifact": "${id.organization} % ${id.name} % ${id.revision}" }""".stripMargin
      )
      Seq(rootFolder / "intellij-compat.json")
    },
    macroAnnotationSettings
  )

lazy val `scala-relay-ijext` = project
  .enablePlugins(SbtIdeaPlugin, Sonatype)
  .settings(org.jetbrains.sbtidea.Keys.buildSettings)
  .settings(commonSettings ++ mavenSettings)
  .settings(
    crossScalaVersions := Seq(Versions.Scala213),
    intellijPluginName := name.value,
    intellijPlugins ++= Seq("org.intellij.scala".toPlugin),
    intellijBuild := (ThisBuild / intellijBuild).value,
    intellijBaseDirectory := (ThisBuild / intellijBaseDirectory).value,
    packageMethod := PackagingMethod.Standalone(), // This only works for proper plugins
    patchPluginXml := pluginXmlOptions { xml =>
      // This only works for proper plugins
      xml.version = version.value
      xml.sinceBuild = (ThisBuild / intellijBuild).value
      xml.untilBuild = "280.*"
    },
    Compile / resourceGenerators += Def.task {
      val rootFolder = (Compile / resourceManaged).value / "META-INF"
      rootFolder.mkdirs()
      val fileOut = rootFolder / "intellij-compat.xml"

      IO.write(fileOut, s"""
          |<!DOCTYPE intellij-compat PUBLIC "Plugin/DTD"
          |        "https://raw.githubusercontent.com/JetBrains/intellij-scala/idea183.x/scala/scala-impl/src/org/jetbrains/plugins/scala/components/libextensions/intellij-compat.dtd">
          |<intellij-compat>
          |    <name>Scala Relay Intellij Support</name>
          |    <description>Expands Slinky relay macros</description>
          |    <version>${version.value}</version>
          |    <vendor>Goodcover</vendor>
          |    <ideaVersion since-build="2020.3.0" until-build="2028.1.0">
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
  organization := "com.goodcover",
  sonatypeProfileName := "com.goodcover",
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
    releaseStepCommandAndRemaining(";+ scala-relay-ijext/updateIntellij ;+ scala-relay-ijext/test"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+ publishSigned"),
    releaseStepCommandAndRemaining("^ sbt-scala-relay/publishSigned"),
    releaseStepCommandAndRemaining("+ scala-relay-ijext/publishSigned"),
    releaseStepCommandAndRemaining("sonatypeReleaseAll"),
    setNextVersion,
    ReleaseCustom.commitNextVersion,
    pushChanges
  )

addCommandAlias("publishLocalAll", ";publishLocal ;scala-relay-ijext/publishLocal")
