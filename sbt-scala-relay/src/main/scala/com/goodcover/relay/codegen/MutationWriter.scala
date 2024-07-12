package com.goodcover.relay.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition
import com.goodcover.relay.GraphQLSchema

import java.io.Writer

class MutationWriter(
  writer: Writer,
  mutation: OperationDefinition,
  documentText: String,
  schema: GraphQLSchema,
  typeMappings: Map[String, String]
) extends OperationWriter(writer, mutation, documentText, schema, typeMappings) {

  override def write(): Unit = {
    writePreamble()
    inputWriter.writeOperationInputTypes()
    writeOperationTrait()
    writeOperationObject()
  }

  override protected val operationObjectParent: String = "MutationTaggedNode"

  override protected def getOperationField(name: String): TypeDefinition.FieldDefinition =
    schema.mutationField(name)
}
