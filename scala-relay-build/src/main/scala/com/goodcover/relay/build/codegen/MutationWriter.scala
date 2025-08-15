package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Document
import com.goodcover.relay.build.GraphQLSchema

import java.io.Writer

class MutationWriter(
  writer: Writer,
  mutation: OperationDefinition,
  documentText: String,
  document: Document,
  schema: GraphQLSchema,
  typeConverter: TypeConverter
) extends OperationWriter(writer, mutation, documentText, document, schema, typeConverter) {

  override protected val operationObjectParent: String = "MutationTaggedNode"
}
