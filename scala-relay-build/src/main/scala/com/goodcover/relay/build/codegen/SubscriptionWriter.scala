package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.FieldDefinition
import caliban.parsing.adt.Document
import com.goodcover.relay.build.GraphQLSchema

import java.io.Writer

class SubscriptionWriter(
  writer: Writer,
  subscription: OperationDefinition,
  documentText: String,
  document: Document,
  schema: GraphQLSchema,
  typeConverter: TypeConverter
) extends OperationWriter(writer, subscription, documentText, document, schema, typeConverter) {

  override def write(): Unit = {
    writePreamble()
    operationInputWriter.writeOperationInputType()
    writeOperationTrait()
    writeOperationObject()
  }

  override protected val operationObjectParent: String = "SubscriptionTaggedNode"

  override protected def getOperationField(name: String): FieldDefinition =
    schema.subscriptionField(name)
}
