package com.goodcover.relay.build

import caliban.parsing.Parser
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition._
import caliban.parsing.adt.Definition.{ExecutableDefinition, TypeSystemDefinition}
import caliban.parsing.adt.Type.NamedType
import caliban.parsing.adt.{Directive, Document, Type}

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class GraphQLSchema(val file: File, val document: Document, additional: Seq[Document]) {

  // See https://spec.graphql.org/October2021.

  // TODO: Change IllegalArgumentExceptions to NoSuchElementExceptions

  // TODO: Handle schema extensions.
  //  You have to pull out the TypeSystemExtensions from the document.definitions manually.

  lazy val schemaDefinition: TypeSystemDefinition.SchemaDefinition =
    document.schemaDefinition.getOrElse(throw invalidSchema("Missing schema."))

  lazy val fragments: Map[String, ExecutableDefinition.FragmentDefinition] =
    additional.foldLeft(document.fragmentDefinitions.map(d => d.name -> d).toMap) { (definitions, document) =>
      definitions ++ document.fragmentDefinitions.map(d => d.name    -> d)
    }

  def fragment(name: String): ExecutableDefinition.FragmentDefinition =
    fragments.getOrElse(name, throw invalidSchema(s"Missing fragment $name."))

  lazy val objectTypes: Map[String, TypeDefinition.ObjectTypeDefinition] =
    additional.foldLeft(document.objectTypeDefinitions.map(d => d.name -> d).toMap) { (definitions, document) =>
      definitions ++ document.objectTypeDefinitions.map(d => d.name    -> d)
    }

  def objectType(name: String): TypeDefinition.ObjectTypeDefinition =
    objectTypes.getOrElse(name, throw invalidSchema(s"Missing type $name."))

  lazy val inputObjectTypes: Map[String, TypeDefinition.InputObjectTypeDefinition] =
    additional.foldLeft(document.inputObjectTypeDefinitions.map(d => d.name -> d).toMap) { (definitions, document) =>
      definitions ++ document.inputObjectTypeDefinitions.map(d => d.name    -> d)
    }

  def inputObjectType(name: String): TypeDefinition.InputObjectTypeDefinition =
    inputObjectTypes.getOrElse(name, throw invalidSchema(s"Missing input $name."))

  lazy val unionTypes: Map[String, TypeDefinition.UnionTypeDefinition] =
    additional.foldLeft(document.unionTypeDefinitions.map(d => d.name -> d).toMap) { (definitions, document) =>
      definitions ++ document.unionTypeDefinitions.map(d => d.name    -> d)
    }

  def unionType(name: String): TypeDefinition.UnionTypeDefinition =
    unionTypes.getOrElse(name, throw invalidSchema(s"Missing union $name."))

  lazy val scalarTypes: Map[String, TypeDefinition.ScalarTypeDefinition] =
    additional.foldLeft(document.scalarTypeDefinitions.map(d => d.name -> d).toMap) { (definitions, document) =>
      definitions ++ document.scalarTypeDefinitions.map(d => d.name    -> d)
    }

  def scalarType(name: String): TypeDefinition.ScalarTypeDefinition =
    scalarTypes.getOrElse(name, throw invalidSchema(s"Missing scalar $name."))

  lazy val enumTypes: Map[String, TypeDefinition.EnumTypeDefinition] =
    additional.foldLeft(document.enumTypeDefinitions.map(d => d.name -> d).toMap) { (definitions, document) =>
      definitions ++ document.enumTypeDefinitions.map(d => d.name    -> d)
    }

  def enumType(name: String): TypeDefinition.EnumTypeDefinition =
    enumTypes.getOrElse(name, throw invalidSchema(s"Missing enum $name."))

  lazy val interfaceTypes: Map[String, TypeDefinition.InterfaceTypeDefinition] =
    additional.foldLeft(document.interfaceTypeDefinitions.map(d => d.name -> d).toMap) { (definitions, document) =>
      definitions ++ document.interfaceTypeDefinitions.map(d => d.name    -> d)
    }

  def interfaceType(name: String): TypeDefinition.InterfaceTypeDefinition =
    interfaceTypes.getOrElse(name, throw invalidSchema(s"Missing interface $name."))

  lazy val directiveDefinitions: Map[String, TypeSystemDefinition.DirectiveDefinition] =
    additional.foldLeft(document.directiveDefinitions.map(d => d.name -> d).toMap) { (definitions, document) =>
      definitions ++ document.directiveDefinitions.map(d => d.name    -> d)
    }

  def directiveDefinition(name: String): TypeSystemDefinition.DirectiveDefinition =
    directiveDefinitions.getOrElse(name, throw invalidSchema(s"Missing directive $name."))

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

  val mutationObjectType: Option[TypeDefinition.ObjectTypeDefinition] = {
    schemaDefinition.mutation.map(objectType)
  }

  lazy val mutationFields: Map[String, TypeDefinition.FieldDefinition] = {
    mutationObjectType.map(_.fields.map(d => d.name -> d).toMap).getOrElse(Map.empty)
  }

  def mutationField(name: String): TypeDefinition.FieldDefinition =
    mutationFields.getOrElse(name, throw new IllegalArgumentException(s"Missing mutation $name."))

  val subscriptionObjectType: Option[TypeDefinition.ObjectTypeDefinition] = {
    schemaDefinition.subscription.map(objectType)
  }

  lazy val subscriptionFields: Map[String, TypeDefinition.FieldDefinition] = {
    subscriptionObjectType.map(_.fields.map(d => d.name -> d).toMap).getOrElse(Map.empty)
  }

  def subscriptionField(name: String): TypeDefinition.FieldDefinition =
    subscriptionFields.getOrElse(name, throw new IllegalArgumentException(s"Missing subscription $name."))

  private def invalidSchema(message: String): GraphQLSchema.InvalidSchema =
    GraphQLSchema.InvalidSchema(file, message)

  def fieldType(name: String): GraphQLSchema.FieldTypeDefinition =
    objectInterfaceUnionType(name)

  def fragmentType(name: String): GraphQLSchema.FieldTypeDefinition =
    objectInterfaceUnionType(name)

  def objectInterfaceUnionType(name: String): GraphQLSchema.FieldTypeDefinition =
    objectTypes
      .get(name)
      .map(GraphQLSchema.FieldTypeDefinition(_))
      .orElse(interfaceTypes.get(name).map(GraphQLSchema.FieldTypeDefinition(_)))
      .orElse(unionTypes.get(name).map(GraphQLSchema.FieldTypeDefinition(_)))
      .getOrElse(throw invalidSchema(s"Missing type, interface, or union $name."))

  private def unsupportedOperation(message: String): GraphQLSchema.UnsupportedOperation =
    GraphQLSchema.UnsupportedOperation(file, message)
}

object GraphQLSchema {

  def apply(schemaFile: File, additional: Set[File]): GraphQLSchema = {
    val document            = readDocument(schemaFile)
    val additionalDocuments = additional.toSeq.map(readDocument)
    new GraphQLSchema(schemaFile, document, additionalDocuments)
  }

  private def readDocument(file: File): Document = {
    Parser.parseQuery(Files.readString(file.toPath, StandardCharsets.UTF_8)) match {
      case Left(value)  => throw new IllegalArgumentException(s"Failed to parse, $file", value)
      case Right(value) => value
    }
  }

  final case class InvalidSchema(file: File, message: String)
      extends Exception(s"Invalid schema file: $file", new Exception(message))

  final case class UnsupportedOperation(file: File, message: String)
      extends Exception(s"Unsupported operation in schema file: $file", new Exception(message))

  // TODO: Rename
  final case class FieldTypeDefinition(
    description: Option[String],
    name: String,
    implementsOrMembers: List[NamedType],
    directives: List[Directive],
    fields: List[TypeDefinition.FieldDefinition]
  )

  object FieldTypeDefinition {

    def apply(obj: TypeDefinition.ObjectTypeDefinition): FieldTypeDefinition =
      FieldTypeDefinition(obj.description, obj.name, obj.implements, obj.directives, obj.fields)

    def apply(int: TypeDefinition.InterfaceTypeDefinition): FieldTypeDefinition =
      FieldTypeDefinition(int.description, int.name, int.implements, int.directives, int.fields)

    def apply(union: TypeDefinition.UnionTypeDefinition): FieldTypeDefinition =
      FieldTypeDefinition(
        union.description,
        union.name,
        union.memberTypes.map(Type.NamedType(_, nonNull = true)),
        union.directives,
        Nil
      )

    def apply(`enum`: TypeDefinition.EnumTypeDefinition): FieldTypeDefinition =
      FieldTypeDefinition(`enum`.description, `enum`.name, Nil, `enum`.directives, Nil)
  }
}
