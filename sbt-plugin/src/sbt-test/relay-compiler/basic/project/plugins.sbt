addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.19")

addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.7.0")

addSbtPlugin("com.dispalt.relay" % "sbt-relay-compiler" % sys.props.getOrElse("plugin.version", sys.error("'plugin.version' environment variable is not set")))
