package com.goodcover.relay.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.FieldDefinition
import caliban.parsing.adt.Document
import com.goodcover.relay.GraphQLSchema
import com.goodcover.relay.GraphQLText.startOfOperation

import java.io.Writer

abstract class OperationWriter(
  writer: Writer,
  operation: OperationDefinition,
  // TODO: GC-3158 - Remove documentText.
  documentText: String,
  document: Document,
  schema: GraphQLSchema,
  typeConverter: TypeConverter
) extends ExecutableDefinitionWriter(writer, documentText, schema, typeConverter) {

  override protected def definitionName: String = DocumentConverter.getOperationName(operation)

  protected val operationInputWriter = new OperationInputWriter(writer, operation, document, typeConverter)

  override protected def containsStartOfDefinition(line: String): Boolean =
    startOfOperation(line, operation)

  // TODO: This is weird.
  protected def writeOperationTrait(): Unit =
    writeDefinitionTrait(operation.selectionSet, getFieldDefinition(getOperationField))

  protected def writeOperationObject(): Unit = {
    // TODO: This is weird.
    val fieldTypeDefinition: FieldTypeLookup = getFieldDefinitionTypeDefinition(getOperationField)

    writer.write("object ")
    val name = definitionName
    writer.write(name)
    writer.write(" extends _root_.com.goodcover.relay.")
    writer.write(operationObjectParent)
    writer.write('[')
    // TODO: It's kinda weird getting the name from here.
    // FIXME: This should use the other overload but it'll probably break stuff.
    writer.write(typeConverter.convertToScalaType(operationInputWriter.operationInputName, fullyQualified = false))
    writer.write(", ")
    writer.write(name)
    // TODO: Ctor is redundant for queries. Only fragments can be plural.
    writer.write("""] {
                   |  type Ctor[T] = T
                   |
                   |""".stripMargin)
    writeNestedTypeNameObject(None, operation.selectionSet, name, "  ", compact = false)
    writeNestedTraits(name, operation.selectionSet, fieldTypeDefinition, name)
    writeFragmentImplicits(name, operation.selectionSet, name)
    operationInputWriter.writeNewInputMethod()
    // This type is type of the graphql`...` tagged template expression, i.e. GraphQLTaggedNode.
    // In v11 it is either ReaderFragment or ConcreteRequest.
    writer.write("  type Query = _root_.com.goodcover.relay.ConcreteRequest\n\n")
    writeGeneratedMapping(writer, name)
  }

  protected def operationObjectParent: String

  protected def getOperationField(name: String): FieldDefinition

}
