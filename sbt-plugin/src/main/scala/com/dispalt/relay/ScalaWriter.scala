package com.dispalt.relay

import caliban.parsing.Parser
import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.{FieldDefinition, ObjectTypeDefinition}
import caliban.parsing.adt.{OperationType, Selection, Type}
import com.dispalt.relay.GraphQLText.appendOperationText
import sbt.*
import sbt.io.Using.fileWriter

import java.io.Writer
import java.nio.charset.StandardCharsets

// TODO: Rename
class ScalaWriter(outputDir: File, schema: GraphQLSchema) {

  // It would be nice to use Scalameta for this but it doesn't support comments which kinda sucks.
  // See https://github.com/scalameta/scalameta/issues/3372.

  def write(file: File): Set[File] = {
    write(IO.read(file, StandardCharsets.UTF_8))
  }

  def write(documentText: String): Set[File] = {
    val document = Parser.parseQuery(documentText).right.get
    document.operationDefinitions.map(writeOperation(documentText, _)).toSet
  }

  private def writeOperation(documentText: String, operation: OperationDefinition): File = {
    operation.operationType match {
      case OperationType.Query        => writeQuery(documentText, operation)
      case OperationType.Mutation     => writeMutation(documentText, operation)
      case OperationType.Subscription => writeSubscription(documentText, operation)
    }
  }

  private def writeQuery(documentText: String, operation: OperationDefinition): File = {
    val file = operationFile(operation)
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      writePreamble(writer, documentText, operation)
      writer.write("\n")
      writeInputType(writer, operation)
      writer.write("\n")
      writeOperationTrait(writer, operation, schema.queryField)
      writer.write("\n")
      writeOperationObject(writer, operation)
    }
    file
  }

  private def writeMutation(documentText: String, operation: OperationDefinition): File = {
    val file = operationFile(operation)
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      writePreamble(writer, documentText, operation)
      writer.write("\n")
      writeInputType(writer, operation)
      writer.write("\n")
      writeOperationTrait(writer, operation, schema.mutationField)
      writer.write("\n")
      writeOperationObject(writer, operation)
    }
    file
  }

  private def writeSubscription(documentText: String, operation: OperationDefinition): File = {
    val file = operationFile(operation)
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      writePreamble(writer, documentText, operation)
      ???
    }
    file
  }

  private def writePreamble(writer: Writer, documentText: String, operation: OperationDefinition): Unit = {
    writer.write(s"""package relay.generated
         |
         |import _root_.scala.scalajs.js
         |import _root_.scala.scalajs.js.|
         |import _root_.scala.scalajs.js.annotation.JSImport
         |
         |""".stripMargin)
    writer.write("/*\n")
    appendOperationText(documentText, operation) { line =>
      writer.write(line.replace("*/", "*\\/"))
      val last = line.lastOption
      if (!last.contains('\n') && !last.contains('\f')) {
        writer.write("\n")
      }
    }
    writer.write("*/\n")
  }

  // TODO: Don't do this. We should create shared types from the schema.
  private def writeInputType(writer: Writer, operation: OperationDefinition): Unit = {
    writer.write("trait ")
    // TODO: When does an operation not have a name?
    val operationName = operation.name.get
    writer.write(operationName)
    writer.write("Input extends js.Object")
    if (operation.variableDefinitions.nonEmpty) {
      writer.write(" {\n")
      operation.variableDefinitions match {
        case input :: Nil =>
          input.variableType match {
            // TODO: Handle non-object types.
            case named: Type.NamedType =>
              schema.inputObjectType(named.name).fields.foreach { field =>
                // TODO: Handle non-object types.
                writeInputField(writer, field.name, field.ofType)
              }
            case list: Type.ListType => ???
          }
        case head :: tail => ???
        case Nil          => ()
      }
      writer.write("}")
    }
    writer.write("\n")
  }

  private def writeOperationTrait(
    writer: Writer,
    operation: OperationDefinition,
    operationField: String => FieldDefinition
  ): Unit = {
    writer.write("trait ")
    // TODO: When does an operation not have a name?
    val operationName = operation.name.get
    writer.write(operationName)
    writer.write(" extends js.Object {\n")
    operation.selectionSet.foreach {
      case field: Selection.Field =>
        writeOperationField(writer, field.name, operationField(field.name).ofType, operationName)
      case spread: Selection.FragmentSpread   => ???
      case fragment: Selection.InlineFragment => ???
    }
    writer.write("}\n")
  }

  private def writeNestedTrait(
    writer: Writer,
    field: Selection.Field,
    subFields: Map[String, FieldDefinition],
    typeName: String,
    typePrefix: String
  ): Unit = {
    def subFieldType(name: String) =
      subFields.getOrElse(name, throw new IllegalArgumentException(s"Type $typeName does not define field $name."))
    writer.write("  trait ")
    writer.write(field.name)
    writer.write(" extends js.Object {\n")
    field.selectionSet.foreach {
      case subField: Selection.Field =>
        writeNestedField(
          writer,
          subField.name,
          subFieldType(subField.name).ofType,
          typePrefix,
          hasSelections = subField.selectionSet.nonEmpty
        )
      case spread: Selection.FragmentSpread   => ???
      case fragment: Selection.InlineFragment => ???
    }
    writer.write("  }\n")
  }

  private def writeOperationObject(writer: Writer, operation: OperationDefinition): Unit = {
    writer.write("object ")
    // TODO: When does an operation not have a name?
    val operationName = operation.name.get
    writer.write(operationName)
    writer.write(" _root_.relay.gql.QueryTaggedNode[")
    writer.write(operationName)
    writer.write("Input, ")
    writer.write(operationName)
    // TODO: Ctor is redundant for queries. Only fragments can be plural.
    writer.write("""] {
                   |  type Ctor[T] = T
                   |
                   |""".stripMargin)
    writeQueryNestedTraits(writer, operation)
    writer.write("\n")
    writer.write("  def newInput(")
    if (operation.variableDefinitions.nonEmpty) {
      ???
    }
    writer.write("): ")
    writer.write(operationName)
    writer.write(" =")
    if (operation.variableDefinitions.nonEmpty) {
      writer.write("\n    ")
    } else {
      writer.write(" ")
    }
    writer.write("js.Dynamic.literal(")
    if (operation.variableDefinitions.nonEmpty) {
      writer.write("\n    ")
    }
    writer.write(").asInstanceOf[")
    writer.write(operationName)
    // This type is type of the graphql`...` tagged template expression, i.e. GraphQLTaggedNode.
    // In v11 it is either ReaderFragment or ConcreteRequest.
    writer.write("""]
                   |
                   |  type Query = _root_.relay.gql.ConcreteRequest
                   |
                   |  @js.native
                   |  @JSImport("__generated__/""".stripMargin)
    // The __generated__ import here should be setup as an alias to the output location of the relay compiler.
    writer.write(operationName)
    writer.write(""".graphql", JSImport.Default)
                   |  private object node extends js.Object
                   |
                   |  lazy val query: Query = node.asInstanceOf[Query]
                   |
                   |  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
                   |}
                   |""".stripMargin)
  }

  private def writeQueryNestedTraits(writer: Writer, definition: OperationDefinition): Unit = {
    definition.selectionSet.foreach {
      case field: Selection.Field =>
        if (field.selectionSet.nonEmpty) {
          // TODO: Deduplicate
          val tpe = schema.queryField(field.name).ofType match {
            // TODO: Handle non-object types.
            case named: Type.NamedType => schema.objectType(named.name)
            case list: Type.ListType   => ???
          }
          writeNestedSelection(writer, field, tpe, "")
        }
      case spread: Selection.FragmentSpread   => ???
      case fragment: Selection.InlineFragment => ???
    }
  }

  private def writeNestedSelection(
    writer: Writer,
    selection: Selection,
    tpe: ObjectTypeDefinition,
    typePrefix: String
  ): Unit = {
    selection match {
      case field: Selection.Field =>
        // TODO: Cache these better?
        // TODO: It is only worth doing this if there are at least 2 selections.
        val subFields = tpe.fields.map(d => d.name -> d).toMap
        // TODO: Deduplicate
        def subFieldType(name: String) = {
          val definition = subFields.getOrElse(
            name,
            throw new IllegalArgumentException(s"Type ${tpe.name} does not define field $name.")
          )
          definition.ofType match {
            // TODO: Handle non-object types.
            case named: Type.NamedType => schema.objectType(named.name)
            case list: Type.ListType   => ???
          }
        }
        field.selectionSet.foreach {
          case subField: Selection.Field =>
            if (subField.selectionSet.nonEmpty) {
              writeNestedSelection(writer, subField, subFieldType(subField.name), field.name.capitalize + typePrefix)
            }
          case spread: Selection.FragmentSpread   => ???
          case fragment: Selection.InlineFragment => ???
        }
        writeNestedTrait(writer, field, subFields, tpe.name, typePrefix)
      case spread: Selection.FragmentSpread   => ???
      case fragment: Selection.InlineFragment => ???
    }
  }

  private def writeInputField(writer: Writer, name: String, tpe: Type): Unit =
    writeField(writer, name, tpe, nameOfType(tpe), "  ")

  private def writeOperationField(writer: Writer, name: String, tpe: Type, operationName: String): Unit = {
    val typeName = s"$operationName.${name.capitalize}"
    writeField(writer, name, tpe, typeName, "  ")
  }

  private def writeNestedField(
    writer: Writer,
    name: String,
    tpe: Type,
    typePrefix: String,
    hasSelections: Boolean
  ): Unit = {
    val typeName = if (hasSelections) typePrefix + name.capitalize else nameOfType(tpe)
    writeField(writer, name, tpe, typeName, "    ")
  }

  private def writeField(writer: Writer, name: String, tpe: Type, typeName: String, indent: String): Unit = {
    writer.write(indent)
    writer.write("val ")
    writer.write(name)
    writer.write(": ")
    writer.write(convertType(typeName))
    if (tpe.nullable) {
      writer.write(" | Null")
    }
    writer.write("\n")
  }

  private def nameOfType(tpe: Type): String =
    tpe match {
      case named: Type.NamedType => named.name
      case list: Type.ListType   => ???
    }

  private def convertType(typeName: String): String =
    typeName match {
      // TODO: Do something better with ID.
      case "ID"  => "String"
      case other => other
    }

  private def operationFile(operation: OperationDefinition) =
    // TODO: When does an operation not have a name?
    outputDir / s"${operation.name.get}.scala"
}
