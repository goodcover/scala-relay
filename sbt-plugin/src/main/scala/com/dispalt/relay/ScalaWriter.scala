package com.dispalt.relay

import caliban.parsing.Parser
import caliban.parsing.adt.Definition.ExecutableDefinition.{FragmentDefinition, OperationDefinition}
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.{FieldDefinition, InputValueDefinition, ObjectTypeDefinition}
import caliban.parsing.adt.{OperationType, Selection, Type, VariableDefinition}
import com.dispalt.relay.GraphQLText.{appendFragmentText, appendOperationText}
import sbt.*
import sbt.io.Using.fileWriter

import java.io.Writer
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec

// TODO: Rename
class ScalaWriter(outputDir: File, schema: GraphQLSchema) {

  // TODO: Remove strip margins

  // It would be nice to use Scalameta for this but it doesn't support comments which kinda sucks.
  // See https://github.com/scalameta/scalameta/issues/3372.

  def write(file: File): Set[File] = {
    write(IO.read(file, StandardCharsets.UTF_8))
  }

  def write(documentText: String): Set[File] = {
    val document   = Parser.parseQuery(documentText).right.get
    val operations = document.operationDefinitions.map(writeOperation(documentText, _)).toSet
    val fragments  = document.fragmentDefinitions.map(writeFragment(documentText, _)).toSet
    operations ++ fragments
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
      writeOperationPreamble(writer, documentText, operation)
      writer.write('\n')
      writeInputType(writer, operation)
      writer.write('\n')
      writeOperationTrait(writer, operation, schema.queryField)
      writer.write('\n')
      def fieldTypeDefinition(name: String) = fieldDefinitionTypeDefinition(schema.queryField(name))
      writeOperationObject(writer, operation, fieldTypeDefinition)
    }
    file
  }

  private def writeMutation(documentText: String, operation: OperationDefinition): File = {
    val file = operationFile(operation)
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      writeOperationPreamble(writer, documentText, operation)
      writer.write('\n')
      writeInputType(writer, operation)
      writer.write('\n')
      writeOperationTrait(writer, operation, schema.mutationField)
      writer.write('\n')
      def fieldTypeDefinition(name: String) = fieldDefinitionTypeDefinition(schema.mutationField(name))
      writeOperationObject(writer, operation, fieldTypeDefinition)
    }
    file
  }

  private def writeSubscription(documentText: String, operation: OperationDefinition): File = {
    val file = operationFile(operation)
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      writeOperationPreamble(writer, documentText, operation)
      ???
    }
    file
  }

  private def writeFragment(documentText: String, fragment: FragmentDefinition): File = {
    val file = fragmentFile(fragment)
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      writeFragmentPreamble(writer, documentText, fragment)
      writer.write('\n')
      // TODO: Deduplicate
      val typeName = fragment.typeCondition.name
      val fields   = schema.objectType(fragment.typeCondition.name).fields.map(d => d.name -> d).toMap
      def selectionDefinition(name: String) =
        fields.getOrElse(name, throw new IllegalArgumentException(s"Type $typeName does not define field $name."))
      writeFragmentTrait(writer, fragment, selectionDefinition)
      writer.write('\n')
      def fieldTypeDefinition(name: String) = fieldDefinitionTypeDefinition(selectionDefinition(name))
      writeFragmentObject(writer, fragment, fieldTypeDefinition)
    }
    file
  }

  private def writeOperationPreamble(writer: Writer, documentText: String, operation: OperationDefinition): Unit =
    writePreamble(writer, appendOperationText(documentText, operation))

  private def writeFragmentPreamble(writer: Writer, documentText: String, fragment: FragmentDefinition): Unit =
    writePreamble(writer, appendFragmentText(documentText, fragment))

  private def writePreamble(writer: Writer, writeDocumentText: (String => Unit) => Unit): Unit = {
    writePackageAndImports(writer)
    writer.write("/*\n")
    writeDocumentText(writeDefinitionTextLine(writer))
    writer.write("*/\n")
  }

  private def writeDefinitionTextLine(writer: Writer)(line: String): Unit = {
    writer.write(line.replace("*/", "*\\/"))
    val last = line.lastOption
    if (!last.contains('\n') && !last.contains('\f')) {
      writer.write('\n')
    }
  }

  private def writePackageAndImports(writer: Writer): Unit =
    writer.write(s"""package relay.generated
         |
         |import _root_.scala.scalajs.js
         |import _root_.scala.scalajs.js.|
         |import _root_.scala.scalajs.js.annotation.JSImport
         |
         |""".stripMargin)

  // TODO: Don't do this. We should create shared types from the schema.
  private def writeInputType(writer: Writer, operation: OperationDefinition): Unit = {
    writer.write("trait ")
    val operationName = getOperationName(operation)
    writer.write(operationName)
    writer.write("Input extends js.Object")
    val inputFields = operationInputFields(operation)
    if (inputFields.nonEmpty) {
      writer.write(" {\n")
      inputFields.foreach { field =>
        writeInputField(writer, field.name, field.ofType)
      }
      writer.write('}')
    }
    writer.write('\n')
  }

  private def writeOperationTrait(
    writer: Writer,
    operation: OperationDefinition,
    selectionField: String => FieldDefinition
  ): Unit =
    writeDefinitionTrait(writer, getOperationName(operation), operation.selectionSet, selectionField)

  private def writeFragmentTrait(
    writer: Writer,
    fragment: FragmentDefinition,
    selectionField: String => FieldDefinition
  ): Unit =
    writeDefinitionTrait(writer, fragment.name, fragment.selectionSet, selectionField)

  private def writeDefinitionTrait(
    writer: Writer,
    name: String,
    selections: List[Selection],
    selectionField: String => FieldDefinition
  ): Unit = {
    writer.write("trait ")
    writer.write(name)
    writer.write(" extends js.Object {\n")
    selections.foreach {
      case field: Selection.Field =>
        writeOperationField(
          writer,
          field.name,
          selectionField(field.name).ofType,
          name,
          hasSelections = field.selectionSet.nonEmpty
        )
      case _ => () // Fragments are handled as implicit conversions.
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
    def subFieldDefinition(name: String) =
      subFields.getOrElse(name, throw new IllegalArgumentException(s"Type $typeName does not define field $name."))
    writer.write("  trait ")
    // TODO: I think this is missing a prefix.
    writer.write(field.name.capitalize)
    writer.write(" extends js.Object {\n")
    field.selectionSet.foreach {
      case subField: Selection.Field =>
        writeNestedField(
          writer,
          subField.name,
          subFieldDefinition(subField.name).ofType,
          typePrefix,
          hasSelections = subField.selectionSet.nonEmpty
        )
      case _ => () // Fragments are handled as implicit conversions.
    }
    writer.write("  }\n")
  }

  private def writeOperationObject(
    writer: Writer,
    operation: OperationDefinition,
    fieldTypeDefinition: String => ObjectTypeDefinition
  ): Unit = {
    writer.write("object ")
    val name = getOperationName(operation)
    writer.write(name)
    writer.write(" extends _root_.relay.gql.")
    operation.operationType match {
      case OperationType.Query => writer.write("QueryTaggedNode[")
      case OperationType.Mutation => writer.write("MutationTaggedNode[")
      case OperationType.Subscription => ???
    }
    writer.write(name)
    writer.write("Input, ")
    writer.write(name)
    // TODO: Ctor is redundant for queries. Only fragments can be plural.
    writer.write("""] {
                   |  type Ctor[T] = T
                   |
                   |""".stripMargin)
    writeNestedTraits(writer, operation, fieldTypeDefinition)
    writer.write('\n')
    writeNewInputMethod(writer, operation)
    // This type is type of the graphql`...` tagged template expression, i.e. GraphQLTaggedNode.
    // In v11 it is either ReaderFragment or ConcreteRequest.
    writer.write("\n  type Query = _root_.relay.gql.ConcreteRequest\n")
    writeGeneratedMapping(writer, name)
  }

  private def writeFragmentObject(
    writer: Writer,
    fragment: FragmentDefinition,
    fieldTypeDefinition: String => ObjectTypeDefinition
  ): Unit = {
    writer.write("object ")
    val name = fragment.name
    writer.write(name)
    writer.write(" extends _root_.relay.gql.FragmentTaggedNode[")
    writer.write(name)
    writer.write("] {\n")
    writer.write("  type Ctor[T] = ")
    if (isPluralFragment(fragment)) writer.write("js.Array[T]")
    else writer.write('T')
    writer.write('\n')
    writeFragmentImplicits(writer, fragment)
    // This type is type of the graphql`...` tagged template expression, i.e. GraphQLTaggedNode.
    // In v11 it is either ReaderFragment or ConcreteRequest.
    writer.write("  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]\n")
    writeGeneratedMapping(writer, name)
  }

  private def writeGeneratedMapping(writer: Writer, name: String): Unit = {
    writer.write("""
                   |  @js.native
                   |  @JSImport("__generated__/""".stripMargin)
    // TODO: Make this configurable.
    // The __generated__ import here should be setup as an alias to the output location of the relay compiler.
    writer.write(name)
    writer.write(""".graphql", JSImport.Default)
                   |  private object node extends js.Object
                   |
                   |  lazy val query: Query = node.asInstanceOf[Query]
                   |
                   |  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
                   |}
                   |""".stripMargin)
  }

  private def writeNestedTraits(
    writer: Writer,
    operation: OperationDefinition,
    fieldTypeDefinition: String => ObjectTypeDefinition
  ): Unit = {
    operation.selectionSet.foreach {
      case field: Selection.Field =>
        if (field.selectionSet.nonEmpty) {
          writeNestedSelection(writer, field, fieldTypeDefinition(field.name), "")
        }
      case _ => () // Fragments are handled as implicit conversions.
    }
  }

  private def writeNestedSelection(
    writer: Writer,
    selection: Selection,
    typeDefinition: ObjectTypeDefinition,
    typePrefix: String
  ): Unit = {
    selection match {
      case field: Selection.Field =>
        // TODO: Cache these better?
        // TODO: It is only worth doing this if there are at least 2 selections.
        // TODO: Deduplicate
        val subFields = typeDefinition.fields.map(d => d.name -> d).toMap
        def subFieldTypeDefinition(name: String) =
          fieldDefinitionTypeDefinition(
            subFields.getOrElse(
              name,
              throw new IllegalArgumentException(s"Type ${typeDefinition.name} does not define field $name.")
            )
          )
        field.selectionSet.foreach {
          case subField: Selection.Field =>
            if (subField.selectionSet.nonEmpty) {
              writeNestedSelection(
                writer,
                subField,
                subFieldTypeDefinition(subField.name),
                field.name.capitalize + typePrefix
              )
            }
          case _ => () // Fragments are handled as implicit conversions.
        }
        writeNestedTrait(writer, field, subFields, typeDefinition.name, typePrefix)
      case _ => () // Fragments are handled as implicit conversions.
    }
  }

  private def writeNewInputMethod(writer: Writer, operation: OperationDefinition): Unit = {
    val operationName = getOperationName(operation)

    val inputFields = operationInputFields(operation)

    def foreachInputField(f: (InputValueDefinition, Boolean) => Unit): Unit = {
      @tailrec
      def loop(fields: List[InputValueDefinition]): Unit = fields match {
        case Nil => ()
        case field :: Nil =>
          f(field, false)
        case field :: tail =>
          f(field, true)
          loop(tail)
      }
      loop(inputFields)
    }

    writer.write("  def newInput(")
    if (inputFields.nonEmpty) {
      writer.write('\n')
      foreachInputField {
        case (field, hasMore) =>
          writeInputFieldParameter(writer, field.name, field.ofType)
          if (hasMore) writer.write(',')
          writer.write('\n')
      }
      writer.write("  ")
    }
    writer.write("): ")
    writer.write(operationName)
    writer.write("Input =")
    if (inputFields.nonEmpty) {
      writer.write("\n    ")
    } else {
      writer.write(' ')
    }
    writer.write("js.Dynamic.literal(")
    if (inputFields.nonEmpty) {
      writer.write('\n')
      foreachInputField {
        case (field, hasMore) =>
          writer.write("""      """")
          writer.write(field.name)
          writer.write("""" -> """)
          writer.write(field.name)
          writer.write(".asInstanceOf[js.Any]")
          if (hasMore) writer.write(',')
          writer.write('\n')
      }
      writer.write("    ")
    }
    writer.write(").asInstanceOf[")
    writer.write(operationName)
    writer.write("Input]\n")
  }

  private def writeFragmentImplicits(writer: Writer, fragment: FragmentDefinition): Unit = {
    fragment.selectionSet.foreach {
      case spread: Selection.FragmentSpread =>
        writer.write("\n  implicit class ")
        writer.write(fragment.name)
        writer.write('2')
        writer.write(spread.name)
        writer.write("Ref(f: ")
        writer.write(fragment.name)
        writer.write(") extends _root_.relay.gql.CastToFragmentRef[")
        writer.write(fragment.name)
        writer.write(", ")
        writer.write(spread.name)
        writer.write("](f) {\n")
        writer.write("    def to")
        writer.write(spread.name)
        writer.write(": _root_.relay.gql.FragmentRef[")
        writer.write(spread.name)
        writer.write("] = castToRef\n")
        writer.write("  }\n")
      case inline: Selection.InlineFragment => throw new NotImplementedError(inline.toString)
      case _                                => ()
    }
    writer.write('\n')
  }

  private def writeInputField(writer: Writer, name: String, tpe: Type): Unit =
    writeField(writer, name, tpe, nameOfType(tpe), "  ")

  private def writeOperationField(
    writer: Writer,
    name: String,
    tpe: Type,
    operationName: String,
    hasSelections: Boolean
  ): Unit = {
    val typeName = if (hasSelections) s"$operationName.${name.capitalize}" else nameOfType(tpe)
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
    writeNameAndType(writer, name, tpe, typeName)
    writer.write('\n')
  }

  // TODO: Can we not use this?
  private def writeVariableParameter(writer: Writer, variable: VariableDefinition): Unit = {
    writer.write("    ")
    writeNameAndType(writer, variable.name, variable.variableType, nameOfType(variable.variableType))
    variable.defaultValue.foreach { value =>
      writer.write(" = ")
      // TODO: Probably need to convert this to Scala.
      writer.write(value.toInputString)
    }
  }

  private def writeInputFieldParameter(writer: Writer, name: String, tpe: Type): Unit = {
    writer.write("    ")
    writeNameAndType(writer, name, tpe, nameOfType(tpe))
    if (tpe.nullable) {
      writer.write(" = null")
    }
  }

  private def writeNameAndType(writer: Writer, name: String, tpe: Type, typeName: String): Unit = {
    writer.write(name)
    writer.write(": ")
    writer.write(convertType(typeName))
    if (tpe.nullable) {
      writer.write(" | Null")
    }
  }

  private def operationInputFields(operation: OperationDefinition) = {
    operation.variableDefinitions match {
      case variable :: Nil =>
        variable.variableType match {
          // TODO: Handle non-object types.
          case named: Type.NamedType => schema.inputObjectType(named.name).fields
          case list: Type.ListType   => throw new NotImplementedError(list.toString)
        }
      case Nil       => Nil
      case variables => throw new NotImplementedError(variables.toString)
    }
  }

  // TODO: Return None if it is not an object?
  private def fieldDefinitionTypeDefinition(field: FieldDefinition): ObjectTypeDefinition =
    field.ofType match {
      case named: Type.NamedType => schema.objectType(named.name)
      case list: Type.ListType   => throw new NotImplementedError(list.toString)
    }

  private def nameOfType(tpe: Type): String =
    tpe match {
      case named: Type.NamedType => named.name
      case list: Type.ListType   => throw new NotImplementedError(list.toString)
    }

  private def convertType(typeName: String): String =
    typeName match {
      // TODO: Do something better with ID.
      case "ID"  => "String"
      case other => other
    }

  private def isPluralFragment(fragment: FragmentDefinition) =
    fragment.directives.exists { directive =>
      directive.name == "relay" && directive.arguments.get("plural").exists(_.toInputString == "true")
    }

  private def operationFile(operation: OperationDefinition) = {
    val name = getOperationName(operation)
    val file = outputDir.getAbsoluteFile / s"$name.scala"
    if (file.exists()) {
      throw new IllegalArgumentException(s"File $file already exists. Ensure that you only have one operation named $name.")
    }
    file
  }

  private def fragmentFile(fragment: FragmentDefinition) = {
    val name = fragment.name
    val file = outputDir.getAbsoluteFile / s"$name.scala"
    if (file.exists()) {
      throw new IllegalArgumentException(s"File $file already exists. Ensure that you only have one fragment named $name.")
    }
    file
  }

  private def getOperationName(operation: OperationDefinition) =
    operation.name.getOrElse(throw new UnsupportedOperationException("Anonymous queries are not not supported."))
}
