package com.goodcover.relay.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition
import caliban.parsing.adt.Document
import com.goodcover.relay.GraphQLSchema

import java.io.Writer

class MutationWriter(
  writer: Writer,
  mutation: OperationDefinition,
  // TODO: GC-3158 - Remove documentText.
  documentText: String,
  document: Document,
  schema: GraphQLSchema,
  typeConverter: TypeConverter
) extends OperationWriter(writer, mutation, documentText, document, schema, typeConverter) {

  override def write(): Unit = {
    writePreamble()
    operationInputWriter.writeOperationInputType()
    writeOperationTrait()
    writeOperationObject()
  }

  override protected val operationObjectParent: String = "MutationTaggedNode"

  override protected def getOperationField(name: String): TypeDefinition.FieldDefinition =
    schema.mutationField(name)
}
