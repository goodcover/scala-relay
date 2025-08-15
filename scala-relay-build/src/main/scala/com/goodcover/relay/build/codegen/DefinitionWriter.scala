package com.goodcover.relay.build.codegen

import com.goodcover.relay.build.GraphQLSchema

import java.io.Writer

abstract class DefinitionWriter(writer: Writer, documentText: String, schema: GraphQLSchema, typeConverter: TypeConverter) {
  
  protected val scalaWriter = new ScalaWriter(writer)
  
  def write(): Unit
  
  protected def writeHeader(): Unit = {
    writer.write("package relay.generated\n\n")
    writer.write("import scala.scalajs.js\n")
    writer.write("import scala.scalajs.js.annotation.JSImport\n\n")
  }
  
  protected def writeFooter(): Unit = {
    // Optional footer content
  }
}

abstract class ExecutableDefinitionWriter(writer: Writer, documentText: String, schema: GraphQLSchema, typeConverter: TypeConverter) 
  extends DefinitionWriter(writer, documentText, schema, typeConverter)
