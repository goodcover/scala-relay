package com.dispalt.relay

import caliban.parsing.Parser
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition
import caliban.parsing.adt.Document
import com.dispalt.relay.GraphQLSchema.{InvalidSchema, UnsupportedOperation}
import sbt.*

import java.nio.charset.StandardCharsets

class GraphQLSchema(file: File, document: Document) {

  // See https://spec.graphql.org/October2021.

  private lazy val schemaDefinition =
    document.schemaDefinition.getOrElse(throw InvalidSchema(file, "Schema definition is undefined."))

  private lazy val objectTypes =
    document.objectTypeDefinitions.map(d => d.name -> d).toMap

  def objectType(name: String): TypeDefinition.ObjectTypeDefinition =
    objectTypes.getOrElse(name, throw InvalidSchema(file, s"Object type $name undefined."))

  private lazy val inputObjectTypes =
    document.inputObjectTypeDefinitions.map(d => d.name -> d).toMap

  def inputObjectType(name: String): TypeDefinition.InputObjectTypeDefinition =
    inputObjectTypes.getOrElse(name, throw InvalidSchema(file, s"Input type $name undefined."))

  lazy val queryObjectType: TypeDefinition.ObjectTypeDefinition = {
    // The query root operation type must be provided and must be an Object type.
    val queryObjectName =
      schemaDefinition.query.getOrElse(throw InvalidSchema(file, "Schema does not define a query root operation type."))
    objectType(queryObjectName)
  }

  private lazy val queryFields = {
    queryObjectType.fields.map(d => d.name -> d).toMap
  }

  def queryField(name: String): TypeDefinition.FieldDefinition =
    queryFields.getOrElse(name, throw new IllegalArgumentException(s"Query $name is undefined."))

  lazy val mutationObjectType: TypeDefinition.ObjectTypeDefinition = {
    // The mutation root operation type is optional; if it is not provided, the service does not support mutations.
    // If it is provided, it must be an Object type.
    val mutationObjectName =
      schemaDefinition.mutation.getOrElse(throw UnsupportedOperation(file, "Schema does not define a mutation root operation type."))
    objectType(mutationObjectName)
  }

  private lazy val mutationFields = {
    mutationObjectType.fields.map(d => d.name -> d).toMap
  }

  def mutationField(name: String): TypeDefinition.FieldDefinition =
    mutationFields.getOrElse(name, throw new IllegalArgumentException(s"Mutation $name is undefined."))

  lazy val subscriptionObjectType: TypeDefinition.ObjectTypeDefinition = {
    // The subscription root operation type is optional; if it is not provided, the service does not support subscriptions.
    // If it is provided, it must be an Object type.
    val subscriptionObjectName =
      schemaDefinition.subscription.getOrElse(throw UnsupportedOperation(file, "Schema does not define a subscription root operation type."))
    objectType(subscriptionObjectName)
  }

  private lazy val subscriptionFields = {
    subscriptionObjectType.fields.map(d => d.name -> d).toMap
  }

  def subscriptionField(name: String): TypeDefinition.FieldDefinition =
    subscriptionFields.getOrElse(name, throw new IllegalArgumentException(s"Subscription $name is undefined."))
}

object GraphQLSchema {

  def apply(file: File): GraphQLSchema = {
    val document = Parser.parseQuery(IO.read(file, StandardCharsets.UTF_8)).right.get
    new GraphQLSchema(file, document)
  }

  final case class InvalidSchema(file: File, message: String)
      extends Exception(s"Invalid schema file: $file", new Exception(message))

  final case class UnsupportedOperation(file: File, message: String)
    extends Exception(s"Unsupported operation in schema file: $file", new Exception(message))
}
