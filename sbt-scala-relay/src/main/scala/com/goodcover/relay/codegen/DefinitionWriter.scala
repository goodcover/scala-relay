package com.goodcover.relay.codegen

import java.io.Writer

abstract class DefinitionWriter(writer: Writer) {

  protected val scalaWriter = new ScalaWriter(writer)

  def write(): Unit

  protected def definitionName: String

  protected def writePreamble(): Unit = {
    writePackageAndImports()
    writer.write("/*\n")
    writeDefinitionText()
    writer.write("*/\n\n")
  }

  private def writePackageAndImports(): Unit = {
    writer.write("package relay.generated\n\n")
    writeImports()
  }

  protected def writeImports(): Unit

  protected def writeDefinitionText(): Unit
}
