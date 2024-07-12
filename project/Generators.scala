import sbt._

object Generators {
  // Generates a scala file that contains the Lagom version for use at runtime.
  def version(version: String, dir: File): Seq[File] = {
    val file = dir / "com" / "goodcover" / "relay" / "core" / "SRCVersion.scala"
    val scalaSource =
      """|package com.goodcover.relay.core
         |
         |object SRCVersion {
         |    val current = "%s"
         |}
        """.stripMargin.format(version)

    if (!file.exists() || IO.read(file) != scalaSource) {
      IO.write(file, scalaSource)
    }

    Seq(file)
  }
}
