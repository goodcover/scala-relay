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
    ???
  }

  override protected val operationObjectParent: String = "TODO"

  override protected def getOperationField(name: String): TypeDefinition.FieldDefinition =
    schema.subscriptionField(name)
}
