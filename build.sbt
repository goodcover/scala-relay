import sbtrelease.ReleaseCustom
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._

// Run scala-relay-ijext/updateIntellij
ThisBuild / updateIntellij := {}
ThisBuild / organization   := "com.goodcover.relay"

ThisBuild / intellijPluginName := "scala-relay-ijext"
// See https://www.jetbrains.com/intellij-repository/releases
// search for com.jetbrains.intellij.idea
ThisBuild / intellijBuild      := "252"

//  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
//  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
//  else localStaging.value
ThisBuild / publishTo := localStaging.value

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions    := Nil,
    publish / skip        := true,
    PgpKeys.publishSigned := {},
    publishLocal          := {},
    publishArtifact       := false,
    publish               := {}
  )
  .aggregate( //
    `sbt-scala-relay`,
    `mill-scala-relay`,
    `scala-relay-build`,
    `scala-relay-core`,
    `scala-relay-macros`
  )

lazy val `scala-relay-build` = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(Dependencies.Caliban, Dependencies.ScalaMeta),
    scalaVersion       := Versions.Scala212,
    crossScalaVersions := Seq(Versions.Scala212, Versions.Scala3)
  )

lazy val `mill-scala-relay` = project
  .settings(commonSettings)
  .dependsOn(`scala-relay-build`)
  .aggregate(`scala-relay-build`)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi"           %% "mill-libs-scalalib" % Versions.Mill,
      Dependencies.millTestkit % Test,
      Dependencies.munit       % Test,
    ),
//    // Force all dependencies to use Scala 3 versions
//    dependencyOverrides ++= Seq(
//      "org.scala-lang.modules" %% "scala-collection-compat" % "2.13.0",
//      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.37.6",
//      "com.lihaoyi" %% "sourcecode" % "0.4.2",
//      "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
//      "org.scalameta" %% "scalameta" % Versions.ScalaMeta,
//      "org.scalameta" %% "parsers" % Versions.ScalaMeta
//    ),
//    // Exclude all Scala 2.13 versions from all dependencies
    excludeDependencies ++= Seq(
      ExclusionRule("org.scala-lang.modules", "scala-collection-compat_2.13"),
      ExclusionRule("com.github.plokhotnyuk.jsoniter-scala", "jsoniter-scala-core_2.13"),
      ExclusionRule("com.lihaoyi", "sourcecode_2.13"),
      ExclusionRule("org.scala-lang.modules", "scala-xml_2.13"),
      ExclusionRule("org.scalameta", "scalameta_2.13"),
      ExclusionRule("org.scalameta", "parsers_2.13")
    ),
    scalaVersion       := Versions.Scala37,
    crossScalaVersions := Seq(Versions.Scala37),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )

lazy val `sbt-scala-relay` = project
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .dependsOn(`scala-relay-build`)
  .settings(commonSettings)
  .settings(
    sbtPlugin            := true,
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion),
    buildInfoKeys        := Seq[BuildInfoKey](version),
    buildInfoPackage     := "com.goodcover.relay",
    scriptedLaunchOpts += "-Dplugin.version=" + version.value,
    scriptedBufferLog    := false,
    scriptedDependencies := {
      scriptedDependencies.value
      (`scala-relay-build` / publishLocal).value
      (`scala-relay-core` / publishLocal).value
      (`scala-relay-macros` / publishLocal).value
    },
    scalaVersion         := Versions.Scala212,
    crossScalaVersions   := Seq(Versions.Scala212)
  )

lazy val `scala-relay-core` = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    commonSettings,
    Compile / resourceGenerators += Def.task {
      val rootFolder = (Compile / resourceManaged).value / "META-INF"
      rootFolder.mkdirs()
      val id         = (`scala-relay-ijext` / projectID).value
      IO.write(
        rootFolder / "intellij-compat.json",
        s"""{"artifact": "${id.organization} % ${id.name}_2.13 % ${id.revision}" }""".stripMargin
      )
      Seq(rootFolder / "intellij-compat.json")
    },
    crossScalaVersions := Seq(Versions.Scala213, Versions.Scala3),
  )

lazy val `scala-relay-macros` = project
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies += Dependencies.ScalaReflect.value,
    macroAnnotationSettings,
    crossScalaVersions := Seq(Versions.Scala213),
  )

lazy val `scala-relay-ijext` = project
  .enablePlugins(SbtIdeaPlugin)
  .settings(org.jetbrains.sbtidea.Keys.buildSettings)
  .settings(commonSettings)
  .settings(
    crossScalaVersions    := Seq(Versions.Scala213),
    intellijPluginName    := name.value,
    intellijPlugins ++= Seq("org.intellij.scala".toPlugin),
    intellijBuild         := (ThisBuild / intellijBuild).value,
    intellijBaseDirectory := (ThisBuild / intellijBaseDirectory).value,
    packageMethod         := PackagingMethod.Standalone(), // This only works for proper plugins
    patchPluginXml        := pluginXmlOptions { xml =>
      xml.version = version.value
      xml.sinceBuild = (ThisBuild / intellijBuild).value
    },
    Compile / resourceGenerators += Def.task {
      val rootFolder = (Compile / resourceManaged).value / "META-INF"
      rootFolder.mkdirs()
      val fileOut    = rootFolder / "intellij-compat.xml"

      IO.write(
        fileOut,
        s"""
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
          """.stripMargin
      )

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
  scalaVersion       := Versions.Scala213,
  crossScalaVersions := Seq(Versions.Scala213),
  scalacOptions ++= {
    val commonOptions = Seq(
      "-feature",
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-unchecked",
      "-release",
      "11",
      "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds",         // Allow higher-kinded types
      "-language:implicitConversions"  // Allow definition of implicit functions called views
    )

    val scala2Options = Seq(
      "-Xlint",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-language:_",
    )

    val scala3Options = Seq(
      "-Wunused:all",
      "-Wvalue-discard"
    )

    if (scalaVersion.value.startsWith("3.")) {
      commonOptions ++ scala3Options
    } else {
      commonOptions ++ scala2Options
    }
  },
  pomExtra           :=
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
  homepage           := Some(url(s"https://github.com/goodcover/scala-relay")),
  licenses           := Seq("MIT License" -> url("http://opensource.org/licenses/mit-license.php")),
  scmInfo            := Some(
    ScmInfo(url("https://github.com/goodcover/scala-relay"), "scm:git:git@github.com:goodcover/scala-relay.git")
  ),
  publishMavenStyle  := true
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
    releaseStepCommandAndRemaining("+ scala-relay-build/publishSigned"),
    releaseStepCommandAndRemaining("+ mill-scala-relay/publishSigned"),
    releaseStepCommandAndRemaining("^ sbt-scala-relay/publishSigned"),
    releaseStepCommandAndRemaining("+ scala-relay-ijext/publishSigned"),
    releaseStepCommand("sonaUpload"),
    setNextVersion,
    ReleaseCustom.commitNextVersion,
    pushChanges,
    ReleaseCustom.createGhRelease
  )

addCommandAlias(
  "publishLocalAll",
  ";publishLocal ;scala-relay-build/publishLocal ;mill-scala-relay/publishLocal ;scala-relay-ijext/publishLocal"
)
