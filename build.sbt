lazy val root =
  project.in(file(".")).aggregate(`relay-compiler`)

lazy val `relay-compiler` = project
  .in(file("sbt-plugin"))
  .settings(
    sbtPlugin := true,
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % Version.Scalajs),
    addSbtPlugin(
      "ch.epfl.scala" % "sbt-scalajs-bundler" % Version.ScalajsBundler),
    ScriptedPlugin.scriptedSettings,
    scriptedLaunchOpts += "-Dplugin.version=" + version.value,
    scriptedBufferLog := false
  )
  .settings(commonSettings)

lazy val `relay-macro` = project
  .in(file("relay-macro"))
  .settings(
    metaMacroSettings,
    commonSettings,
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

lazy val commonSettings = Seq(
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
