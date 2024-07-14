addSbtPlugin(
  "com.goodcover" % "sbt-scala-relay" % sys.props
    .getOrElse("plugin.version", sys.error("'plugin.version' environment variable is not set"))
)

addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1")
