package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.FragmentDefinition
import caliban.parsing.adt.Document
import com.goodcover.relay.build.GraphQLSchema
import com.goodcover.relay.build.codegen.Directives.Refetchable

import java.io.Writer
import scala.meta.Type

class RefetchableFragmentWriter(
  writer: Writer,
  fragment: FragmentDefinition,
  refetchable: Refetchable,
  documentText: String,
  document: Document,
  schema: GraphQLSchema,
  typeConverter: TypeConverter
) extends ExecutableDefinitionWriter(writer, documentText, schema, typeConverter) {

  override protected def definitionName: String = refetchable.queryName

  override def write(): Unit = {
    writeHeader()
    writeRefetchableQueryTrait()
    writeRefetchableQueryObject()
    writeFooter()
  }

  private def writeRefetchableQueryTrait(): Unit = {
    val typeName = Type.Name(refetchable.queryName)

    // For now, create a simple trait
    scalaWriter.writeTrait(
      tname = typeName,
      parentTraits = Seq.empty,
      fields = Seq.empty,
      jsNative = true,
      indent = ""
    ) { _ => }
  }

  private def writeRefetchableQueryObject(): Unit = {
    val inputTypeName = refetchable.queryName + "Input"

    writer.write("object ")
    writer.write(refetchable.queryName)
    writer.write(" extends _root_.com.goodcover.relay.QueryTaggedNode[")
    writer.write(inputTypeName)
    writer.write(", ")
    writer.write(refetchable.queryName)
    writer.write("] {\n")
    writer.write("  type Ctor[T] = T\n")
    writer.write("  \n")
    writer.write("  val node: String = \"\"\"\n")
    writer.write(documentText)
    writer.write("\n  \"\"\"\n")
    writer.write("}\n")
  }
}
