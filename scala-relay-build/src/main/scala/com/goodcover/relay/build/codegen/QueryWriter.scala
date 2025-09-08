package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition
import caliban.parsing.adt.Document
import com.goodcover.relay.build.GraphQLSchema

import java.io.Writer

class QueryWriter(
  writer: Writer,
  query: OperationDefinition,
  // TODO: GC-3158 - Remove documentText.
  documentText: String,
  document: Document,
  schema: GraphQLSchema,
  typeConverter: TypeConverter
) extends OperationWriter(writer, query, documentText, document, schema, typeConverter) {

  override def write(): Unit = {
    writePreamble()
    operationInputWriter.writeOperationInputType()
    writeOperationTrait()
    writeOperationObject()
  }

  override protected val operationObjectParent: String = "QueryTaggedNode"

  override protected def getOperationField(name: String): TypeDefinition.FieldDefinition =
    schema.queryField(name)
}
