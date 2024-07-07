package com.dispalt.relay

import caliban.parsing.Parser
import caliban.parsing.adt.Definition.TypeSystemDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition._
import caliban.parsing.adt.Type.NamedType
import caliban.parsing.adt.{Directive, Document, Type}
import com.dispalt.relay.GraphQLSchema.{FieldTypeDefinition, InvalidSchema, UnsupportedOperation}
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

  lazy val unionTypes: Map[String, TypeDefinition.UnionTypeDefinition] =
    document.unionTypeDefinitions.map(d => d.name -> d).toMap

  def unionType(name: String): TypeDefinition.UnionTypeDefinition =
    unionTypes.getOrElse(name, throw invalidSchema(s"Missing union $name."))

  lazy val enumTypes: Map[String, TypeDefinition.EnumTypeDefinition] =
    document.enumTypeDefinitions.map(d => d.name -> d).toMap

  def enumType(name: String): TypeDefinition.EnumTypeDefinition =
    enumTypes.getOrElse(name, throw invalidSchema(s"Missing enum $name."))

  lazy val interfaceTypes: Map[String, TypeDefinition.InterfaceTypeDefinition] =
    document.interfaceTypeDefinitions.map(d => d.name -> d).toMap

  def interfaceType(name: String): TypeDefinition.InterfaceTypeDefinition =
    interfaceTypes.getOrElse(name, throw invalidSchema(s"Missing interface $name."))

  def fieldType(name: String): FieldTypeDefinition =
    objectTypes
      .get(name)
      .map(FieldTypeDefinition(_))
      .orElse(interfaceTypes.get(name).map(FieldTypeDefinition(_)))
      .orElse(unionTypes.get(name).map(FieldTypeDefinition(_)))
      // TODO: Currently these are implemented as String.
      //.orElse(enumTypes.get(name).map(FieldTypeDefinition(_)))
      .getOrElse(throw invalidSchema(s"Missing type, interface, union, or enum $name."))

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

  final case class FieldTypeDefinition(
    description: Option[String],
    name: String,
    implementsOrMembers: List[NamedType],
    directives: List[Directive],
    fields: List[FieldDefinition]
  )

  object FieldTypeDefinition {

    def apply(obj: ObjectTypeDefinition): FieldTypeDefinition =
      FieldTypeDefinition(obj.description, obj.name, obj.implements, obj.directives, obj.fields)

    def apply(int: InterfaceTypeDefinition): FieldTypeDefinition =
      FieldTypeDefinition(int.description, int.name, int.implements, int.directives, int.fields)

    def apply(union: UnionTypeDefinition): FieldTypeDefinition =
      FieldTypeDefinition(
        union.description,
        union.name,
        union.memberTypes.map(Type.NamedType(_, nonNull = true)),
        union.directives,
        Nil
      )

    def apply(enum: EnumTypeDefinition): FieldTypeDefinition =
      FieldTypeDefinition(enum.description, enum.name, Nil, enum.directives, Nil)
  }
}
