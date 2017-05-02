import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, _}
import sbtrelease.ReleaseStateTransformations._

lazy val root =
  project
    .in(file("."))
    .settings(
      PgpKeys.publishSigned := {},
      publishLocal := {},
      publishArtifact in Compile := false,
      publish := {}
    )
    .aggregate(`sbt-relay-compiler`, `relay-macro`)

def RuntimeLibPlugins =
  Sonatype && CrossPerProjectPlugin && PluginsAccessor.exclude(BintrayPlugin)
def SbtPluginPlugins = BintrayPlugin && PluginsAccessor.exclude(Sonatype)

lazy val `sbt-relay-compiler` = project
  .in(file("sbt-plugin"))
  .enablePlugins(SbtPluginPlugins)
  .settings(
    sbtPlugin := true,
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % Version.Scalajs),
    addSbtPlugin(
      "ch.epfl.scala" % "sbt-scalajs-bundler" % Version.ScalajsBundler),
    ScriptedPlugin.scriptedSettings,
    scriptedLaunchOpts += "-Dplugin.version=" + version.value,
    scriptedBufferLog := false
  )
  .settings(
    commonSettings,
    publishTo := {
      if (isSnapshot.value) {
        // Bintray doesn't support publishing snapshots, publish to Sonatype snapshots instead
        Some(Opts.resolver.sonatypeSnapshots)
      } else publishTo.value
    },
    publishMavenStyle := isSnapshot.value
  )

lazy val `relay-macro` = project
  .in(file("relay-macro"))
  .enablePlugins(RuntimeLibPlugins)
  .settings(
    metaMacroSettings,
    commonSettings,
    publishMavenStyle := true,
    scalaVersion := Version.Scala211,
    crossScalaVersions := Seq(Version.Scala211, Version.Scala212),
    libraryDependencies ++= Seq(
      Library.sangria
    )
  )

lazy val metaMacroSettings: Seq[Def.Setting[_]] = Seq(
  // A dependency on macro paradise 3.x is required to both write and expand
  // new-style macros.  This is similar to how it works for old-style macro
  // annotations and a dependency on macro paradise 2.x.
  addCompilerPlugin(
    "org.scalameta" % "paradise" % "3.0.0-M8" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  libraryDependencies += Library.scalameta,
  // temporary workaround for https://github.com/scalameta/paradise/issues/10
  scalacOptions in (Compile, console) := Seq(), // macroparadise plugin doesn't work in repl yet.
  // temporary workaround for https://github.com/scalameta/paradise/issues/55
  sources in (Compile, doc) := Nil // macroparadise doesn't work with scaladoc yet.
)

def bintraySettings: Seq[Setting[_]] = Seq(
  bintrayOrganization := Some("dispalt"),
  bintrayRepository := "sbt-plugins",
  bintrayPackage := "sbt-relay-compiler",
  bintrayReleaseOnPublish := true
)

lazy val releaseSettings = Seq(
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseCrossBuild := false,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    releaseStepCommandAndRemaining("+test"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommandAndRemaining("+sonatypeReleaseAll"),
    pushChanges
  )
)

lazy val commonSettings = bintraySettings ++ releaseSettings ++ Seq(
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
  ),
  organization := "com.dispalt.relay",
  sonatypeProfileName := "com.dispalt.relay",
  pomExtra :=
    <developers>
        <developer>
          <id>dispalt</id>
          <name>Dan Di Spaltro</name>
          <url>http://dispalt.com</url>
        </developer>
      </developers>,
  homepage := Some(url(s"https://github.com/scalacenter/scalajs-bundler")),
  licenses := Seq(
    "MIT License" -> url("http://opensource.org/licenses/mit-license.php")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/dispalt/XXX"),
      "scm:git:git@github.com:dispalt/XXX.git"
    )
  )
)
