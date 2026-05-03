package com.goodcover.relay.build.codegen

import java.io.Writer

abstract class DefinitionWriter(writer: Writer, nativeUnionTypes: Boolean) {

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

  protected def writeScalaJsImports(importJSImport: Boolean): Unit = {
    writer.write("import _root_.scala.scalajs.js\n")
    if (!nativeUnionTypes) {
      writer.write("import _root_.scala.scalajs.js.|\n")
    }
    if (importJSImport) {
      writer.write("import _root_.scala.scalajs.js.annotation.JSImport\n")
    }
    writer.write('\n')
  }
}
