package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.FragmentDefinition
import com.goodcover.relay.build.GraphQLSchema

import java.io.Writer
import scala.meta.Type

class FragmentWriter(
  writer: Writer,
  fragment: FragmentDefinition,
  documentText: String,
  schema: GraphQLSchema,
  typeConverter: TypeConverter
) extends ExecutableDefinitionWriter(writer, documentText, schema, typeConverter) {

  override def write(): Unit = {
    writeHeader()
    writeFragmentTrait()
    writeFragmentObject()
    writeFooter()
  }
  
  private def writeFragmentTrait(): Unit = {
    val typeName = Type.Name(fragment.name)
    
    // For now, create a simple trait
    scalaWriter.writeTrait(
      tname = typeName,
      parentTraits = Seq.empty,
      fields = Seq.empty,
      jsNative = true,
      indent = ""
    ) { _ => }
  }
  
  private def writeFragmentObject(): Unit = {
    writer.write("object ")
    writer.write(fragment.name)
    writer.write(" extends _root_.com.goodcover.relay.FragmentTaggedNode[")
    writer.write(fragment.name)
    writer.write("] {\n")
    writer.write("  type Ctor[T] = T\n")
    writer.write("  \n")
    writer.write("  val node: String = \"\"\"\n")
    writer.write(documentText)
    writer.write("\n  \"\"\"\n")
    writer.write("}\n")
  }
}
