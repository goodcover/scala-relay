package com.goodcover.relay.build.codegen

import caliban.InputValue
import caliban.InputValue.{ListValue, ObjectValue, VariableValue}
import caliban.parsing.Parser
import caliban.parsing.adt.Definition.ExecutableDefinition.{FragmentDefinition, OperationDefinition}
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.InputObjectTypeDefinition
import caliban.parsing.adt.Type.{innerType, NamedType}
import caliban.parsing.adt._
import com.goodcover.relay.build.FileOps._
import com.goodcover.relay.build.{BuildLogger, GraphQLConverter, GraphQLSchema}
import com.goodcover.relay.build.codegen.Directives.{getRefetchable, Refetchable}
import com.goodcover.relay.build.codegen.DocumentConverter._

import java.io.{File, Writer}
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class DocumentConverter(outputDir: File, schema: GraphQLSchema, typeMappings: Map[String, String], outputs: Set[File]) {

  private val typeConverter = new TypeConverter(schema, typeMappings)

  def convertSchema(): Set[File] = {
    val inputs = schema.inputObjectTypes.values.map(writeInput(_, schema.document)).toSet
    inputs
  }

  def convert(file: File): Set[File] = {
    convert(Files.readString(file.toPath, StandardCharsets.UTF_8))
  }

  def convert(documentText: String): Set[File] = {
    val document = Parser.parseQuery(documentText).right.get
    // TODO: GC-3158 - Remove documentText from these.
    val operations = document.operationDefinitions.map(writeOperation(_, documentText, document)).toSet
    val fragments  = document.fragmentDefinitions.map(writeFragment(_, documentText)).toSet
    val refetchables = {
      for {
        fragment    <- document.fragmentDefinitions
        refetchable <- getRefetchable(fragment.directives)
      } yield writeRefetchableFragment(fragment, refetchable, documentText, document)
    }.toSet
    operations ++ fragments ++ refetchables
  }

  private def writeInput(input: InputObjectTypeDefinition, document: Document): File =
    writeDefinition(inputFile(input))(new InputWriter(_, input, document, typeConverter))

  private def writeOperation(operation: OperationDefinition, documentText: String, document: Document): File =
    writeDefinition(operationFile(operation)) { writer =>
      operation.operationType match {
        case OperationType.Query =>
          new QueryWriter(writer, operation, documentText, document, schema, typeConverter)
        case OperationType.Mutation =>
          new MutationWriter(writer, operation, documentText, document, schema, typeConverter)
        case OperationType.Subscription =>
          new SubscriptionWriter(writer, operation, documentText, document, schema, typeConverter)
      }
    }

  private def writeFragment(fragment: FragmentDefinition, documentText: String): File =
    writeDefinition(fragmentFile(fragment))(new FragmentWriter(_, fragment, documentText, schema, typeConverter))

  private def writeRefetchableFragment(
    fragment: FragmentDefinition,
    refetchable: Refetchable,
    documentText: String,
    document: Document
  ): File = {
    val variables = refetchableFragmentVariables(fragment)
    // TODO: Add directives.
    val directives = Nil
    val selection  = Selection.FragmentSpread(fragment.name, Nil)
    val query =
      OperationDefinition(OperationType.Query, Some(refetchable.queryName), variables, directives, List(selection))
    // FIXME: This doesn't adequately check for duplicate outputs.
    // FIXME: The definition text is missing because it is looking for the wrong thing.
    writeOperation(query, documentText, document)
  }

  private def writeDefinition(file: File)(f: Writer => DefinitionWriter): File =
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      f(writer).write()
      file
    }

  private def inputFile(input: InputObjectTypeDefinition) =
    outputFile(input.name)

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

object DocumentConverter {

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

  def convertSimple(
    graphqlFiles: Set[File],
    schemaFile: File,
    dependencies: Set[File],
    options: GraphQLConverter.Options,
    logger: BuildLogger
  ): Set[File] = {
    logger.info("Running GraphQLConverter...")

    if (!schemaFile.exists()) {
      logger.error(s"Schema file does not exist: $schemaFile")
      return Set.empty
    }

    try {
      // Parse the schema
      val schemaText = scala.io.Source.fromFile(schemaFile)(StandardCharsets.UTF_8).mkString
      val schemaDocument = Parser.parseQuery(schemaText) match {
        case Right(doc) => doc
        case Left(error) =>
          logger.error(s"Failed to parse schema file $schemaFile: $error")
          return Set.empty
      }

      val schema    = GraphQLSchema(schemaFile, Set.empty)
      val converter = new DocumentConverter(options.outputDir, schema, options.typeMappings, Set.empty)

      // Convert schema input types
      val schemaResults = converter.convertSchema()

      // Convert GraphQL operation files
      val operationResults = graphqlFiles.flatMap { graphqlFile =>
        try {
          logger.debug(s"Converting GraphQL file: ${graphqlFile.getName}")
          converter.convert(graphqlFile)
        } catch {
          case e: Exception =>
            logger.warn(s"Failed to convert ${graphqlFile.getPath}: ${e.getMessage}")
            Set.empty[File]
        }
      }

      schemaResults ++ operationResults
    } catch {
      case e: Exception =>
        logger.error(s"Failed to convert GraphQL files: ${e.getMessage}")
        Set.empty
    }
  }

}
