package com.dispalt.relay

import caliban.parsing.Parser
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition
import caliban.parsing.adt.Document
import com.dispalt.relay.GraphQLSchema.InvalidSchema
import sbt.*

import java.nio.charset.StandardCharsets

class GraphQLSchema(file: File, document: Document) {

  private lazy val schemaDefinition =
    document.schemaDefinition.getOrElse(throw InvalidSchema(file, "Schema definition is undefined."))

  private lazy val objectTypes =
    document.objectTypeDefinitions.map(d => d.name -> d).toMap

  private def objectType(name: String) =
    objectTypes.getOrElse(name, throw InvalidSchema(file, s"Object type $name undefined."))

  private lazy val queryFields = {
    // The query root operation type must be provided and must be an Object type.
    val queryObjectName =
      schemaDefinition.query.getOrElse(throw InvalidSchema(file, "Schema definition does not define query."))
    objectType(queryObjectName).fields.map(d => d.name -> d).toMap
  }

  def queryField(name: String): TypeDefinition.FieldDefinition =
    queryFields.getOrElse(name, throw new IllegalArgumentException(s"Query $name is undefined."))
}

object GraphQLSchema {

  def apply(file: File): GraphQLSchema = {
    val document = Parser.parseQuery(IO.read(file, StandardCharsets.UTF_8)).right.get
    new GraphQLSchema(file, document)
  }

  final case class InvalidSchema(file: File, message: String)
      extends Exception(s"Invalid schema file: $file", new Exception(message))
}
