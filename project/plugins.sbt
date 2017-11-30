addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.21")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.4")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
//addSbtPlugin("com.eed3si9n" % "sbt-doge" % "0.1.5")
