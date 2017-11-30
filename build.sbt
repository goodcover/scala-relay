import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, _}
import sbtrelease.ReleaseStateTransformations._

crossSbtVersions := List("0.13.16", "1.0.4")

lazy val root =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(PgpKeys.publishSigned := {}, publishLocal := {}, publishArtifact in Compile := false, publish := {})
//    .enablePlugins(CrossPerProjectPlugin)
    .aggregate(`sbt-relay-compiler`, `relay-macro`)

def RuntimeLibPlugins = Sonatype && PluginsAccessor.exclude(BintrayPlugin)
def SbtPluginPlugins  = BintrayPlugin && PluginsAccessor.exclude(Sonatype)

lazy val `sbt-relay-compiler` = project
  .in(file("sbt-plugin"))
  .enablePlugins(SbtPluginPlugins)
  .enablePlugins(CrossPerProjectPlugin)
  .settings(commonSettings)
  .settings(sbtPlugin := true,
            addSbtPlugin("org.scala-js"  % "sbt-scalajs"         % Version.Scalajs),
            addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % Version.ScalajsBundler),
            ScriptedPlugin.scriptedSettings,
            scriptedLaunchOpts += "-Dplugin.version=" + version.value,
            scriptedBufferLog := false,
            publishTo := {
              if (isSnapshot.value) {
                // Bintray doesn't support publishing snapshots, publish to Sonatype snapshots instead
                Some(Opts.resolver.sonatypeSnapshots)
              } else publishTo.value
            },
            scriptedDependencies := {
              val () = scriptedDependencies.value
              val () = publishLocal.value
              val () = (publishLocal in `relay-macro`).value
            },
            crossSbtVersions := List("0.13.16", "1.0.4"),
            scalaVersion := {
              (sbtBinaryVersion in pluginCrossBuild).value match {
                case "0.13" => "2.10.6"
                case _      => "2.12.4"
              }
            },
//            // sbt dependent libraries
//            libraryDependencies ++= {
//              (sbtVersion in pluginCrossBuild).value match {
//                case v if v.startsWith("1.") => Seq("org.scala-sbt" %% "io" % "1.0.0")
//                case _                       => Seq()
//              }
//            },
            // fixed in https://github.com/sbt/sbt/pull/3397 (for sbt 0.13.17)
            sbtBinaryVersion in update := (sbtBinaryVersion in pluginCrossBuild).value,
            publishMavenStyle := isSnapshot.value,
            sourceGenerators in Compile += Def.task {
              Generators.version(version.value, (sourceManaged in Compile).value)
            }.taskValue,
            releaseProcess :=
              Seq[ReleaseStep](checkSnapshotDependencies,
                               inquireVersions,
                               runClean,
                               releaseStepCommandAndRemaining("+test"),
                               setReleaseVersion,
                               commitReleaseVersion,
                               tagRelease,
                               releaseStepTask(bintrayRelease in thisProjectRef.value),
                               setNextVersion,
                               commitNextVersion,
                               pushChanges))

lazy val `relay-macro` = project
  .in(file("relay-macro"))
  .enablePlugins(RuntimeLibPlugins && ScalaJSPlugin)
  .enablePlugins(CrossPerProjectPlugin)
  .settings(publishMavenStyle := true,
            scalaVersion := Version.Scala212,
            crossScalaVersions := Seq(Version.Scala211, Version.Scala212),
            libraryDependencies ++= Seq(Library.sangria % Provided, Library.scalatest))
  .settings(metaMacroSettings)
  .settings(commonSettings)

lazy val metaMacroSettings: Seq[Def.Setting[_]] =
  Seq(
      // A dependency on macro paradise 3.x is required to both write and expand
      // new-style macros.  This is similar to how it works for old-style macro
      // annotations and a dependency on macro paradise 2.x.
      addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
      scalacOptions += "-Xplugin-require:macroparadise",
      libraryDependencies += Library.scalameta,
      // temporary workaround for https://github.com/scalameta/paradise/issues/10
      scalacOptions in (Compile, console) := Seq(), // macroparadise plugin doesn't work in repl yet.
      // temporary workaround for https://github.com/scalameta/paradise/issues/55
      sources in (Compile, doc) := Nil // macroparadise doesn't work with scaladoc yet.
  )

lazy val bintraySettings: Seq[Setting[_]] =
  Seq(bintrayOrganization := Some("dispalt"),
      bintrayRepository := "sbt-plugins",
      bintrayPackage := "sbt-relay-compiler",
      bintrayReleaseOnPublish := true)

lazy val releaseSettings =
  Seq(releasePublishArtifactsAction := PgpKeys.publishSigned.value,
      releaseCrossBuild := false,
      releaseProcess :=
        Seq[ReleaseStep](checkSnapshotDependencies,
                         inquireVersions,
                         runClean,
                         releaseStepCommandAndRemaining("+test"),
                         setReleaseVersion,
                         commitReleaseVersion,
                         tagRelease,
                         releaseStepCommandAndRemaining("+publishSigned"),
                         releaseStepCommandAndRemaining("sonatypeReleaseAll"),
                         setNextVersion,
                         commitNextVersion,
                         pushChanges))

lazy val commonSettings = bintraySettings ++ releaseSettings ++ Seq(
  scalacOptions ++= Seq("-feature",
                        "-deprecation",
                        "-encoding",
                        "UTF-8",
                        "-unchecked",
                        "-Xlint",
                        "-Yno-adapted-args",
                        "-Ywarn-dead-code",
                        "-Ywarn-numeric-widen",
                        "-Ywarn-value-discard",
                        "-Xfuture"),
  organization := "com.dispalt.relay",
  sonatypeProfileName := "com.dispalt",
  pomExtra :=
    <developers>
        <developer>
          <id>dispalt</id>
          <name>Dan Di Spaltro</name>
          <url>http://dispalt.com</url>
        </developer>
      </developers>,
  homepage := Some(url(s"https://github.com/dispalt/relay-modern-helper")),
  licenses := Seq("MIT License" -> url("http://opensource.org/licenses/mit-license.php")),
  scmInfo := Some(
    ScmInfo(url("https://github.com/dispalt/relay-modern-helper"),
            "scm:git:git@github.com:dispalt/relay-modern-helper.git")))
