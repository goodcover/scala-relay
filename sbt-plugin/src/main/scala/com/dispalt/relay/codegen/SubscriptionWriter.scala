package com.dispalt.relay.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition
import com.dispalt.relay.GraphQLSchema

import java.io.Writer

class SubscriptionWriter(
  writer: Writer,
  subscription: OperationDefinition,
  documentText: String,
  schema: GraphQLSchema,
  typeMappings: Map[String, String]
) extends OperationWriter(writer, subscription, documentText, schema, typeMappings) {

  override def write(): Unit = {
    writePreamble()
    inputWriter.writeOperationInputTypes()
    writeOperationTrait()
    writeOperationObject()
  }

  override protected val operationObjectParent: String = "SubscriptionTaggedNode"

  override protected def getOperationField(name: String): TypeDefinition.FieldDefinition =
    schema.subscriptionField(name)
}
