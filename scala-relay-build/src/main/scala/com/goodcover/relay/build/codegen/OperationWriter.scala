package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Document
import com.goodcover.relay.build.GraphQLSchema

import java.io.Writer
import scala.meta.Type

abstract class OperationWriter(
  writer: Writer,
  operation: OperationDefinition,
  documentText: String,
  document: Document,
  schema: GraphQLSchema,
  typeConverter: TypeConverter
) extends ExecutableDefinitionWriter(writer, documentText, schema, typeConverter) {

  protected def operationName: String = operation.name.getOrElse("UnnamedOperation")
  
  protected def operationObjectParent: String
  
  override def write(): Unit = {
    writeHeader()
    writeOperationInputType()
    writeOperationTrait()
    writeOperationObject()
    writeFooter()
  }
  
  protected def writeOperationInputType(): Unit = {
    val inputTypeName = operationName + "Input"
    val typeName = Type.Name(inputTypeName)
    
    // For now, create an empty input type
    scalaWriter.writeTrait(
      tname = typeName,
      parentTraits = Seq.empty,
      fields = Seq.empty,
      jsNative = true,
      indent = ""
    ) { _ => }
    
    // Write companion object
    writer.write("object ")
    writer.write(inputTypeName)
    writer.write(" {\n")
    writer.write("  def apply(): ")
    writer.write(inputTypeName)
    writer.write(" = js.Dynamic.literal().asInstanceOf[")
    writer.write(inputTypeName)
    writer.write("]\n")
    writer.write("}\n\n")
  }
  
  protected def writeOperationTrait(): Unit = {
    val typeName = Type.Name(operationName)
    
    // For now, create a simple trait
    scalaWriter.writeTrait(
      tname = typeName,
      parentTraits = Seq.empty,
      fields = Seq.empty,
      jsNative = true,
      indent = ""
    ) { _ => }
  }
  
  protected def writeOperationObject(): Unit = {
    val inputTypeName = operationName + "Input"
    
    writer.write("object ")
    writer.write(operationName)
    writer.write(" extends _root_.com.goodcover.relay.")
    writer.write(operationObjectParent)
    writer.write('[')
    writer.write(inputTypeName)
    writer.write(", ")
    writer.write(operationName)
    writer.write("] {\n")
    writer.write("  type Ctor[T] = T\n")
    writer.write("  \n")
    writer.write("  val node: String = \"\"\"\n")
    writer.write(documentText)
    writer.write("\n  \"\"\"\n")
    writer.write("}\n")
  }
}
