package com.dispalt.relay

import caliban.parsing.Parser
import caliban.parsing.adt.Definition.TypeSystemDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.{FieldDefinition, InterfaceTypeDefinition, ObjectTypeDefinition}
import caliban.parsing.adt.Type.NamedType
import caliban.parsing.adt.{Directive, Document}
import com.dispalt.relay.GraphQLSchema.{InvalidSchema, ObjectOrInterfaceTypeDefinition, UnsupportedOperation}
import sbt._

import java.nio.charset.StandardCharsets

class GraphQLSchema(file: File, document: Document) {

  // See https://spec.graphql.org/October2021.

  lazy val schemaDefinition: TypeSystemDefinition.SchemaDefinition =
    document.schemaDefinition.getOrElse(throw invalidSchema("Missing schema."))

  lazy val objectTypes: Map[String, TypeDefinition.ObjectTypeDefinition] =
    document.objectTypeDefinitions.map(d => d.name -> d).toMap

  def objectType(name: String): TypeDefinition.ObjectTypeDefinition =
    objectTypes.getOrElse(name, throw invalidSchema(s"Missing type $name."))

  lazy val inputObjectTypes: Map[String, TypeDefinition.InputObjectTypeDefinition] =
    document.inputObjectTypeDefinitions.map(d => d.name -> d).toMap

  def inputObjectType(name: String): TypeDefinition.InputObjectTypeDefinition =
    inputObjectTypes.getOrElse(name, throw invalidSchema(s"Missing input $name."))

  lazy val interfaceTypes: Map[String, TypeDefinition.InterfaceTypeDefinition] =
    document.interfaceTypeDefinitions.map(d => d.name -> d).toMap

  def interfaceType(name: String): TypeDefinition.InterfaceTypeDefinition =
    interfaceTypes.getOrElse(name, throw invalidSchema(s"Missing interface $name."))

  def objectOrInterfaceType(name: String): ObjectOrInterfaceTypeDefinition =
    objectTypes
      .get(name)
      .map(ObjectOrInterfaceTypeDefinition(_))
      .orElse(interfaceTypes.get(name).map(ObjectOrInterfaceTypeDefinition(_)))
      .getOrElse(throw invalidSchema(s"Missing type or interface $name."))

  val queryObjectType: TypeDefinition.ObjectTypeDefinition = {
    // The query root operation type must be provided and must be an Object type.
    val queryObjectName =
      schemaDefinition.query.getOrElse(throw invalidSchema("Schema does not define a query root operation type."))
    objectType(queryObjectName)
  }

  lazy val queryFields: Map[String, TypeDefinition.FieldDefinition] = {
    queryObjectType.fields.map(d => d.name -> d).toMap
  }

  def queryField(name: String): TypeDefinition.FieldDefinition =
    queryFields.getOrElse(name, throw new IllegalArgumentException(s"Missing query $name."))

  lazy val mutationObjectType: TypeDefinition.ObjectTypeDefinition = {
    // The mutation root operation type is optional; if it is not provided, the service does not support mutations.
    // If it is provided, it must be an Object type.
    val mutationObjectName =
      schemaDefinition.mutation.getOrElse(
        throw UnsupportedOperation(file, "Schema does not define a mutation root operation type.")
      )
    objectType(mutationObjectName)
  }

  lazy val mutationFields: Map[String, TypeDefinition.FieldDefinition] = {
    mutationObjectType.fields.map(d => d.name -> d).toMap
  }

  def mutationField(name: String): TypeDefinition.FieldDefinition =
    mutationFields.getOrElse(name, throw new IllegalArgumentException(s"Missing mutation $name."))

  lazy val subscriptionObjectType: TypeDefinition.ObjectTypeDefinition = {
    // The subscription root operation type is optional; if it is not provided, the service does not support subscriptions.
    // If it is provided, it must be an Object type.
    val subscriptionObjectName =
      schemaDefinition.subscription.getOrElse(
        throw UnsupportedOperation(file, "Schema does not define a subscription root operation type.")
      )
    objectType(subscriptionObjectName)
  }

  lazy val subscriptionFields: Map[String, TypeDefinition.FieldDefinition] = {
    subscriptionObjectType.fields.map(d => d.name -> d).toMap
  }

  def subscriptionField(name: String): TypeDefinition.FieldDefinition =
    subscriptionFields.getOrElse(name, throw new IllegalArgumentException(s"Missing subscription $name."))

  def invalidSchema(message: String): InvalidSchema =
    InvalidSchema(file, message)
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

  final case class ObjectOrInterfaceTypeDefinition(
    description: Option[String],
    name: String,
    implements: List[NamedType],
    directives: List[Directive],
    fields: List[FieldDefinition]
  )

  object ObjectOrInterfaceTypeDefinition {

    def apply(obj: ObjectTypeDefinition): ObjectOrInterfaceTypeDefinition =
      ObjectOrInterfaceTypeDefinition(obj.description, obj.name, obj.implements, obj.directives, obj.fields)

    def apply(int: InterfaceTypeDefinition): ObjectOrInterfaceTypeDefinition =
      ObjectOrInterfaceTypeDefinition(int.description, int.name, int.implements, int.directives, int.fields)
  }
}
