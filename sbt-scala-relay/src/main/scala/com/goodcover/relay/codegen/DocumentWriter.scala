package com.goodcover.relay.codegen

import caliban.InputValue
import caliban.InputValue.{ListValue, ObjectValue, VariableValue}
import caliban.parsing.Parser
import caliban.parsing.adt.Definition.ExecutableDefinition.{FragmentDefinition, OperationDefinition}
import caliban.parsing.adt.Type.{NamedType, innerType}
import caliban.parsing.adt.{OperationType, Selection, VariableDefinition}
import com.goodcover.relay.GraphQLSchema
import com.goodcover.relay.codegen.Directives.{Refetchable, getRefetchable}
import com.goodcover.relay.codegen.DocumentWriter.{getOperationName, variableArguments}
import sbt._
import sbt.io.Using.fileWriter

import java.io.Writer
import java.nio.charset.StandardCharsets

class DocumentWriter(outputDir: File, schema: GraphQLSchema, typeMappings: Map[String, String], outputs: Set[File]) {

  def write(file: File): Set[File] = {
    write(IO.read(file, StandardCharsets.UTF_8))
  }

  def write(documentText: String): Set[File] = {
    val document   = Parser.parseQuery(documentText).right.get
    val operations = document.operationDefinitions.map(writeOperation(documentText, _)).toSet
    val fragments  = document.fragmentDefinitions.map(writeFragment(documentText, _)).toSet
    val refetchables = {
      for {
        fragment    <- document.fragmentDefinitions
        refetchable <- getRefetchable(fragment.directives)
      } yield writeRefetchableFragment(documentText, fragment, refetchable)
    }.toSet
    operations ++ fragments ++ refetchables
  }

  private def writeOperation(documentText: String, operation: OperationDefinition): File = {
    operation.operationType match {
      case OperationType.Query        => writeQuery(documentText, operation)
      case OperationType.Mutation     => writeMutation(documentText, operation)
      case OperationType.Subscription => writeSubscription(documentText, operation)
    }
  }

  private def writeQuery(documentText: String, query: OperationDefinition): File =
    writeDefinition(operationFile(query))(new QueryWriter(_, query, documentText, schema, typeMappings))

  private def writeMutation(documentText: String, mutation: OperationDefinition): File =
    writeDefinition(operationFile(mutation))(new MutationWriter(_, mutation, documentText, schema, typeMappings))

  private def writeSubscription(documentText: String, subscription: OperationDefinition): File =
    writeDefinition(operationFile(subscription))(
      new SubscriptionWriter(_, subscription, documentText, schema, typeMappings)
    )

  private def writeFragment(documentText: String, fragment: FragmentDefinition): File =
    writeDefinition(fragmentFile(fragment))(new FragmentWriter(_, fragment, documentText, schema, typeMappings))

  private def writeRefetchableFragment(
    documentText: String,
    fragment: FragmentDefinition,
    refetchable: Refetchable
  ): File = {
    val variables = refetchableFragmentVariables(fragment)
    // TODO: Add directives.
    val directives = Nil
    val selection  = Selection.FragmentSpread(fragment.name, Nil)
    val query =
      OperationDefinition(OperationType.Query, Some(refetchable.queryName), variables, directives, List(selection))
    // FIXME: This doesn't adequately check for duplicate outputs.
    // FIXME: The definition text is missing because it is looking for the wrong thing.
    writeDefinition(operationFile(query))(new QueryWriter(_, query, documentText, schema, typeMappings))
  }

  private def writeDefinition(file: File)(f: Writer => ExecutableDefinitionWriter): File =
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      f(writer).write()
      file
    }

  private def operationFile(operation: OperationDefinition) =
    outputFile(getOperationName(operation))

  private def fragmentFile(fragment: FragmentDefinition) =
    outputFile(fragment.name)

  private def outputFile(name: String) = {
    val file = outputDir.getAbsoluteFile / s"$name.scala"
    if (file.exists()) {
      if (outputs.contains(file))
        throw new IllegalArgumentException(
          s"File $file already exists. Ensure that you only have one fragment or operation named $name."
        )
      else file.delete()
    }
    file
  }

  // TODO: This should also take into account any @argumentDefinitions on the fragment.
  private def refetchableFragmentVariables(fragment: FragmentDefinition): List[VariableDefinition] = {
    val typeName = innerType(fragment.typeCondition)
    val tpe      = schema.fragmentType(typeName)
    // The @refetchable directive can only be on fragments with a type condition of Query, Viewer, or Node.
    // The later is quite relaxed and will work on any interface and it will assume that it is queryable via node.
    val implied = if (schema.queryObjectType.name == tpe.name || typeName == "Viewer") {
      Map.empty[String, VariableDefinition]
    } else {
      Map("id" -> VariableDefinition("id", NamedType("ID", nonNull = true), None, Nil))
    }
    val fieldLookup = tpe.fields.map(field => field.name -> field).toMap
    val inner       = selectionVariables(fragment.selectionSet, fieldLookup.get, typeName)
    (implied ++ inner).values.toList
  }

  // TODO: There ought to be a way to abstract this sort of traversal. It is painful...
  private def selectionVariables(
    selections: List[Selection],
    fieldLookup: FieldLookup,
    // TODO: Rename.
    parentTypeName: String
  ): Map[String, VariableDefinition] = {
    selections.foldLeft(Map.empty[String, VariableDefinition]) {
      case (args, field: Selection.Field) if Fields.isMetaField(field.name) => args
      case (args, field: Selection.Field) =>
        val definition = fieldLookup(field.name)
          .getOrElse(throw new NoSuchElementException(s"$parentTypeName does not have a field ${field.name}."))
        val argsLookup = definition.args.map(arg => arg.name -> arg).toMap
        val typeName   = innerType(definition.ofType)
        val fieldArgs  = variableArguments(field.arguments, argsLookup.get, typeName)
        val nextArgs   = args ++ fieldArgs
        if (field.selectionSet.nonEmpty) {
          val nextFieldLookup: FieldLookup = schema.fieldType(typeName).fields.map(f => f.name -> f).toMap.get
          nextArgs ++ selectionVariables(field.selectionSet, nextFieldLookup, typeName)
        } else nextArgs
      case (args, inline: Selection.InlineFragment) =>
        val tpe                          = inline.typeCondition.getOrElse(NamedType(parentTypeName, nonNull = true))
        val typeName                     = innerType(tpe)
        val nextFieldLookup: FieldLookup = schema.fieldType(typeName).fields.map(f => f.name -> f).toMap.get
        args ++ selectionVariables(inline.selectionSet, nextFieldLookup, typeName)
      case (args, spread: Selection.FragmentSpread) =>
        val definition                   = schema.fragment(spread.name)
        val typeName                     = innerType(definition.typeCondition)
        val nextFieldLookup: FieldLookup = schema.fragmentType(typeName).fields.map(f => f.name -> f).toMap.get
        args ++ selectionVariables(definition.selectionSet, nextFieldLookup, typeName)
    }
  }
}

object DocumentWriter {

  // TODO: Where should this go?
  private[codegen] def getOperationName(operation: OperationDefinition) =
    operation.name.getOrElse(throw new UnsupportedOperationException("Anonymous operations are not not supported."))

  private def variableArguments(
    arguments: Map[String, InputValue],
    argLookup: ArgLookup,
    typeName: String
  ): Map[String, VariableDefinition] = {
    arguments.collect {
      case (argName, VariableValue(variableName)) =>
        argLookup(argName).fold(throw new NoSuchElementException(s"$typeName does not have an argument $argName.")) {
          arg =>
            variableName -> VariableDefinition(variableName, arg.ofType, None, Nil)
        }
      // TODO: Look inside lists and objects too.
      case (_, ListValue(_)) =>
        throw new NotImplementedError("Variables within lists for refetchable fragments have not been implemented.")
      case (_, ObjectValue(_)) =>
        throw new NotImplementedError("Variables within objects for refetchable fragments have not been implemented.")
    }
  }
}
