lazy val root =
  project.in(file(".")).aggregate(`sbt-plugin`)


lazy val `sbt-plugin` = project
  .in(file("sbt-plugin"))
  .settings(
    sbtPlugin := true,
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % Version.Scalajs),
    addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % Version.ScalajsBundler)
  )


lazy val metaMacroSettings: Seq[Def.Setting[_]] = Seq(
  // A dependency on macro paradise 3.x is required to both write and expand
  // new-style macros.  This is similar to how it works for old-style macro
  // annotations and a dependency on macro paradise 2.x.
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M8" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  // temporary workaround for https://github.com/scalameta/paradise/issues/10
  scalacOptions in (Compile, console) := Seq(), // macroparadise plugin doesn't work in repl yet.
  // temporary workaround for https://github.com/scalameta/paradise/issues/55
  sources in (Compile, doc) := Nil // macroparadise doesn't work with scaladoc yet.
)
