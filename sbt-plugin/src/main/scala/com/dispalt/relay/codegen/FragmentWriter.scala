package com.dispalt.relay.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.FragmentDefinition
import com.dispalt.relay.GraphQLSchema
import com.dispalt.relay.GraphQLText.startOfFragment
import com.dispalt.relay.codegen.Directives.isPlural

import java.io.Writer

class FragmentWriter(
  writer: Writer,
  fragment: FragmentDefinition,
  documentText: String,
  schema: GraphQLSchema,
  typeMappings: Map[String, String]
) extends ExecutableDefinitionWriter(writer, documentText, schema, typeMappings) {

  private val typeName = fragment.typeCondition.name
  private val fields   = schema.fieldType(typeName).fields.map(d => d.name -> d).toMap

  override protected def definitionName: String = fragment.name

  override def write(): Unit = {
    writePreamble()
    writeFragmentTrait()
    writeFragmentObject()
  }

  override protected def containsStartOfDefinition(line: String): Boolean =
    startOfFragment(line, fragment)

  private def writeFragmentTrait(): Unit =
    writeDefinitionTrait(fragment.selectionSet, getFieldDefinition(typeName, fields))

  private def writeFragmentObject(): Unit = {
    val fieldTypeDefinition = getFieldDefinitionTypeDefinition(typeName, fields)

    writer.write("object ")
    val name = fragment.name
    writer.write(name)
    writer.write(" extends _root_.relay.gql.FragmentTaggedNode[")
    writer.write(name)
    writer.write("] {\n")
    writer.write("  type Ctor[T] = ")
    if (isPlural(fragment.directives)) writer.write("js.Array[T]")
    else writer.write('T')
    writer.write("\n\n")
    writeNestedTypeNameObject(None, fragment.selectionSet, name, "  ")
    writeNestedTraits(fragment.typeCondition.name, fragment.selectionSet, fieldTypeDefinition, name)
    writeFragmentImplicits(fragment.name, fragment.selectionSet, name)
    // This type is type of the graphql`...` tagged template expression, i.e. GraphQLTaggedNode.
    // In v11 it is either ReaderFragment or ConcreteRequest.
    writer.write("  type Query = _root_.relay.gql.")
    if (Directives.isInline(fragment.directives)) writer.write("ReaderInlineDataFragment")
    else writer.write("ReaderFragment")
    writer.write("[Ctor, Out]\n")
    writeGeneratedMapping(writer, name)
  }
}
