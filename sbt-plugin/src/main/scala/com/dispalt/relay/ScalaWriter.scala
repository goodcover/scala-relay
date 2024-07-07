package com.dispalt.relay

import caliban.parsing.Parser
import caliban.parsing.adt.Definition.ExecutableDefinition.{FragmentDefinition, OperationDefinition}
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.{FieldDefinition, InputValueDefinition}
import caliban.parsing.adt.Type.innerType
import caliban.parsing.adt.{OperationType, Selection, Type, VariableDefinition}
import com.dispalt.relay.GraphQLSchema.ObjectOrInterfaceTypeDefinition
import com.dispalt.relay.GraphQLText.{appendFragmentText, appendOperationText}
import sbt._
import sbt.io.Using.fileWriter

import java.io.Writer
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec

// TODO: Rename

class ScalaWriter(outputDir: File, schema: GraphQLSchema, outputs: Set[File]) {

  // TODO: Remove strip margins

  // It would be nice to use Scalameta for this but it doesn't support comments which kinda sucks.
  // See https://github.com/scalameta/scalameta/issues/3372.

  private type FieldLookup     = String => Option[FieldDefinition]
  private type FieldTypeLookup = String => Option[ObjectOrInterfaceTypeDefinition]

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
      writeInputType(writer, operation)
      writeOperationTrait(writer, operation, getFieldDefinition(schema.queryField))
      writeOperationObject(
        writer,
        operation,
        schema.queryObjectType.name,
        getFieldDefinitionTypeDefinition(schema.queryField)
      )
    }
    file
  }

  private def writeMutation(documentText: String, operation: OperationDefinition): File = {
    val file = operationFile(operation)
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      writeOperationPreamble(writer, documentText, operation)
      writeInputType(writer, operation)
      writeOperationTrait(writer, operation, getFieldDefinition(schema.mutationField))
      writeOperationObject(
        writer,
        operation,
        schema.mutationObjectType.name,
        getFieldDefinitionTypeDefinition(schema.mutationField)
      )
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
      val typeName = fragment.typeCondition.name
      val fields   = schema.objectOrInterfaceType(typeName).fields.map(d => d.name -> d).toMap
      writeFragmentTrait(writer, fragment, getFieldDefinition(typeName, fields))
      writeFragmentObject(writer, fragment, getFieldDefinitionTypeDefinition(typeName, fields))
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
    writer.write("*/\n\n")
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

  // TODO: Don't do this. We should create the input types from the schema and share them.
  private def writeInputType(writer: Writer, operation: OperationDefinition): Unit = {
    val name                    = getOperationName(operation) + "Input"
    val singleInputObjectFields = operationSingleInputObjectFields(operation)
    // FIXME: Core uses an empty object instead of null.
    //if (singleInputObjectFields.nonEmpty) {
    operation.selectionSet
    writeTrait(writer, name, singleInputObjectFields, "", Seq.empty, jsNative = false) { field =>
      writeInputField(writer, field.name, field.ofType)
    }
    //}
  }

  private def writeOperationTrait(writer: Writer, operation: OperationDefinition, selectionField: FieldLookup): Unit =
    writeDefinitionTrait(writer, getOperationName(operation), operation.selectionSet, selectionField)

  private def writeFragmentTrait(writer: Writer, fragment: FragmentDefinition, selectionField: FieldLookup): Unit =
    writeDefinitionTrait(writer, fragment.name, fragment.selectionSet, selectionField)

  private def writeDefinitionTrait(
    writer: Writer,
    name: String,
    selections: List[Selection],
    selectionField: FieldLookup
  ): Unit = {
    writeSelectionTrait(writer, name, selections, "", Seq.empty) { field =>
      selectionField(field.name).foreach { definition =>
        writeOperationField(
          writer,
          field.alias.getOrElse(field.name),
          definition.ofType,
          name,
          hasSelections = field.selectionSet.nonEmpty
        )
      }
    }
  }

  private def writeOperationObject(
    writer: Writer,
    operation: OperationDefinition,
    // TODO: Remove.
    typeName: String,
    fieldTypeDefinition: FieldTypeLookup
  ): Unit = {
    writer.write("object ")
    val name = getOperationName(operation)
    writer.write(name)
    writer.write(" extends _root_.relay.gql.")
    operation.operationType match {
      case OperationType.Query        => writer.write("QueryTaggedNode[")
      case OperationType.Mutation     => writer.write("MutationTaggedNode[")
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
    writeNestedTypeNameObject(writer, None, operation.selectionSet, name, "  ")
    writeNestedTraits(writer, name, operation.selectionSet, fieldTypeDefinition, name)
    writeFragmentImplicits(writer, name, operation.selectionSet, name)
    writeNewInputMethod(writer, operation)
    // This type is type of the graphql`...` tagged template expression, i.e. GraphQLTaggedNode.
    // In v11 it is either ReaderFragment or ConcreteRequest.
    writer.write("\n  type Query = _root_.relay.gql.ConcreteRequest\n")
    writeGeneratedMapping(writer, name)
  }

  private def writeFragmentObject(
    writer: Writer,
    fragment: FragmentDefinition,
    fieldTypeDefinition: FieldTypeLookup
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
    writer.write("\n\n")
    writeNestedTypeNameObject(writer, None, fragment.selectionSet, name, "  ")
    writeNestedTraits(writer, fragment.typeCondition.name, fragment.selectionSet, fieldTypeDefinition, name)
    writeFragmentImplicits(writer, fragment.name, fragment.selectionSet, name)
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
    enclosingType: String,
    selections: List[Selection],
    fieldTypeDefinition: FieldTypeLookup,
    outerObjectName: String
  ): Unit = {
    selections.foreach {
      case field: Selection.Field =>
        if (field.selectionSet.nonEmpty) {
          fieldTypeDefinition(field.name).foreach { definition =>
            writeNestedSelection(writer, field, definition, "", outerObjectName, outerObjectName)
          }
        }
      case inline: Selection.InlineFragment =>
        // Inline fragments may also be used to apply a directive to a group of fields. If the TypeCondition is omitted,
        // an inline fragment is considered to be of the same type as the enclosing context.
        // nonNull because we can only get an instance of this via the enclosing type which must exist first.
        val typeName   = inline.typeCondition.fold(enclosingType)(_.name)
        val definition = schema.objectOrInterfaceType(typeName)
        writeNestedSelection(writer, inline, definition, "", outerObjectName, outerObjectName)
      case _: Selection.FragmentSpread => () // Fragment spreads are handled as implicit conversions.
    }
  }

  private def writeNestedSelection(
                                    writer: Writer,
                                    selection: Selection,
                                    typeDefinition: ObjectOrInterfaceTypeDefinition,
                                    enclosingFullName: String,
                                    outerObjectName: String,
                                    inlineParent: String
  ): Unit = {
    // TODO: Cache these better? It is only worth doing this if there are at least 2 selections.
    val subFieldLookup = typeDefinition.fields.map(d => d.name -> d).toMap
    val subSelections = selection match {
      case field: Selection.Field           => field.selectionSet
      case inline: Selection.InlineFragment => inline.selectionSet
      case _: Selection.FragmentSpread      => Nil
    }
    val fullName = selection match {
      case field: Selection.Field => enclosingFullName + field.alias.getOrElse(field.name).capitalize
      // FIXME: This isn't guaranteed to be unique.
      //  For example, you would get a collision if you have a selection on field user and an inline fragment on User.
      case _: Selection.InlineFragment => enclosingFullName + typeDefinition.name.capitalize
      case _: Selection.FragmentSpread => enclosingFullName // Unused.
    }
    subSelections.foreach {
      case subField: Selection.Field =>
        if (subField.selectionSet.nonEmpty) {
          getFieldDefinitionTypeDefinition(typeDefinition.name, subFieldLookup)(subField.name).foreach { definition =>
            writeNestedSelection(writer, subField, definition, fullName, outerObjectName, fullName)
          }
        }
      case inline: Selection.InlineFragment =>
        if (inline.selectionSet.nonEmpty) {
          val definition =
            inline.typeCondition.map(tpe => schema.objectOrInterfaceType(tpe.name)).getOrElse(typeDefinition)
          writeNestedSelection(writer, inline, definition, fullName, outerObjectName, fullName)
        }
      case _: Selection.FragmentSpread =>
        () // Fragment spreads are handled as implicit conversions.
    }
    selection match {
      case field: Selection.Field =>
        writeNestedTrait(writer, fullName, field.selectionSet, subFieldLookup, typeDefinition.name, Seq.empty)
        writeNestedInlineCompanionObject(writer, fullName, field.selectionSet, outerObjectName)
      case inline: Selection.InlineFragment =>
        // FIXME: If there are multiple inline fragments without a type condition we will generate conflicting members.
        val selectionObject = inline.typeCondition.fold(typeDefinition.name)(_.name)
        writeNestedTrait(writer, fullName, inline.selectionSet, subFieldLookup, selectionObject, Seq(inlineParent))
        writeNestedInlineCompanionObject(writer, fullName, inline.selectionSet, outerObjectName)
      case _: Selection.FragmentSpread =>
        () // Fragment spreads are handled as implicit conversions.
    }
  }

  private def writeNestedInlineCompanionObject(
    writer: Writer,
    fullName: String,
    selections: List[Selection],
    outerObjectName: String
  ): Unit = {
    val inlines = inlineFragmentSelections(selections)
    if (inlines.nonEmpty) {
      writer.write("  object ")
      writer.write(fullName)
      writer.write(" {\n")
      writeNestedTypeNameObject(writer, Some(fullName), selections, outerObjectName, "    ")
      writer.write("  }\n\n")
    }
  }

  private def writeNestedTypeNameObject(
    writer: Writer,
    innerObjectName: Option[String],
    selections: List[Selection],
    outerObjectName: String,
    indent: String
  ): Unit = {
    val inlines = inlineFragmentSelections(selections)
    if (inlines.nonEmpty) {
      writer.write(indent)
      writer.write("object __typename {\n")
      inlines.foreach { inline =>
        // FIXME: If there are multiple inline fragments without a type condition we will generate conflicting members.
        val name = inline.typeCondition.fold(innerObjectName.getOrElse(outerObjectName))(_.name)
        writeTypeName(writer, name, s"$outerObjectName.${innerObjectName.getOrElse("")}$name", indent + "  ")
        // TODO: This is pretty silly. I don't know why relay compiler outputs this for TypeScript.
        //  If it is never going to be this then why do they have a type for it?
        //  Check this again once we upgrade.
      }
      writeTypeName(
        writer,
        "`%other`",
        innerObjectName.fold(outerObjectName)(n => s"$outerObjectName.$n"),
        indent + "  "
      )
      writer.write(indent)
      writer.write("}\n\n")
    }
  }

  private def writeTypeName(writer: Writer, name: String, typeName: String, indent: String): Unit = {
    writer.write(indent)
    writer.write("@js.native sealed trait ")
    writer.write(name)
    writer.write(" extends _root_.relay.gql.Introspectable.TypeName[")
    writer.write(typeName)
    writer.write("]\n")
    writer.write(indent)
    writer.write("@inline def ")
    writer.write(name)
    writer.write(": ")
    writer.write(name)
    writer.write(""" = """")
    writer.write(name.stripPrefix("`").stripSuffix("`"))
    writer.write("""".asInstanceOf[""")
    writer.write(name)
    writer.write("]\n")
  }

  private def writeNestedTrait(
    writer: Writer,
    fullName: String,
    selections: List[Selection],
    subFieldLookup: Map[String, FieldDefinition],
    selectionObject: String,
    parentTraits: Seq[String]
  ): Unit = {
    val subFieldDefinition = getFieldDefinition(selectionObject, subFieldLookup)(_)
    writeSelectionTrait(writer, fullName, selections, "  ", parentTraits) { subField =>
      subFieldDefinition(subField.name).foreach { definition =>
        writeNestedField(
          writer,
          subField.alias.getOrElse(subField.name),
          definition.ofType,
          fullName,
          hasSelections = subField.selectionSet.nonEmpty
        )
      }
    }
  }

  private def writeSelectionTrait(
    writer: Writer,
    name: String,
    selections: List[Selection],
    indent: String,
    parentTraits: Seq[String]
  )(f: Selection.Field => Unit): Unit = {
    val parents =
      if (hasTypeName(selections)) parentTraits :+ s"_root_.relay.gql.Introspectable[$name]" else parentTraits
    val fieldSelections = nonMetaFieldSelections(selections)
    writeTrait(writer, name, fieldSelections, indent, parents, jsNative = true)(f)
  }

  private def writeTrait[A](
    writer: Writer,
    name: String,
    fields: Iterable[A],
    indent: String,
    parentTraits: Seq[String],
    jsNative: Boolean
  )(f: A => Unit): Unit = {
    if (jsNative) {
      writer.write(indent)
      writer.write("@js.native\n")
    }
    writer.write(indent)
    writer.write("trait ")
    writer.write(name)
    val parents = if (parentTraits.isEmpty) Seq("js.Object") else parentTraits
    if (parents.nonEmpty) {
      parents match {
        case base +: tail =>
          writer.write(" extends ")
          writer.write(base)
          tail.foreach { parent =>
            writer.write("with ")
            writer.write(parent)
            writer.write(' ')
          }
        case Seq() => ()
      }
    }
    if (fields.nonEmpty) {
      writer.write(" {\n")
      fields.foreach(f)
      writer.write(indent)
      writer.write('}')
    }
    writer.write("\n\n")
  }

  private def writeNewInputMethod(writer: Writer, operation: OperationDefinition): Unit = {
    val singleInputObjectFields = operationSingleInputObjectFields(operation)

    // FIXME: Core uses an empty object instead of null.
    //if (singleInputObjectFields.nonEmpty) {
    val operationName = getOperationName(operation)

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

      loop(singleInputObjectFields)
    }

    writer.write("  def newInput(")
    if (singleInputObjectFields.nonEmpty) {
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
    if (singleInputObjectFields.nonEmpty) {
      writer.write("\n    ")
    } else {
      writer.write(' ')
    }
    writer.write("js.Dynamic.literal(")
    if (singleInputObjectFields.nonEmpty) {
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
    //}
  }

  private def writeFragmentImplicits(
    writer: Writer,
    name: String,
    selections: List[Selection],
    outerObjectName: String,
    outer: Boolean = true
  ): Unit = {
    val typePrefix = if (outer) "" else name
    selections.foreach {
      case field: Selection.Field =>
        val nextName = typePrefix + field.alias.getOrElse(field.name).capitalize
        writeFragmentImplicits(writer, nextName, field.selectionSet, outerObjectName, outer = false)
      case spread: Selection.FragmentSpread =>
        writeImplicitCastToFragmentRef(writer, name, spread)
      case inline: Selection.InlineFragment =>
        val nextName = typePrefix + inline.typeCondition.fold(name)(_.name)
        writeFragmentImplicits(writer, nextName, inline.selectionSet, outerObjectName, outer = false)
    }
    val inlines = inlineFragmentSelections(selections)
    writeInlineFragmentOps(writer, name, inlines, typePrefix)
  }

  private def writeImplicitCastToFragmentRef(writer: Writer, name: String, spread: Selection.FragmentSpread): Unit = {
    writer.write("  implicit class ")
    writer.write(name)
    writer.write('2')
    writer.write(spread.name)
    writer.write("Ref(f: ")
    writer.write(name)
    writer.write(") extends _root_.relay.gql.CastToFragmentRef[")
    writer.write(name)
    writer.write(", ")
    writer.write(spread.name)
    writer.write("](f) {\n")
    writer.write("    def to")
    writer.write(spread.name)
    writer.write(": _root_.relay.gql.FragmentRef[")
    writer.write(spread.name)
    writer.write("] = castToRef\n")
    writer.write("  }\n\n")
  }

  private def writeInlineFragmentOps(
    writer: Writer,
    name: String,
    inlines: List[Selection.InlineFragment],
    typePrefix: String
  ): Unit = {
    if (inlines.nonEmpty) {
      writer.write("  implicit class ")
      writer.write(name)
      // TODO: This underscore is ugly...
      writer.write("_Ops(f: ")
      writer.write(name)
      writer.write(") {\n")
      inlines.foreach { inline =>
        // FIXME: If there are multiple inline fragments without a type condition we will generate conflicting members.
        val to = inline.typeCondition.fold(name)(_.name)
        writer.write("    def as")
        writer.write(to)
        writer.write(": Option[")
        writer.write(typePrefix)
        writer.write(to)
        writer.write("] = _root_.relay.gql.Introspectable.as(f, ")
        writer.write(name)
        writer.write(".__typename.")
        writer.write(to)
        writer.write(")\n")
      }
      writer.write("  }\n\n")
    }
  }

  // TODO: Default values
  private def writeInputField(writer: Writer, name: String, tpe: Type): Unit =
    writeField(writer, name, tpe, innerType(tpe), "  ")

  private def writeOperationField(
    writer: Writer,
    name: String,
    tpe: Type,
    operationName: String,
    hasSelections: Boolean
  ): Unit = {
    val typeName = if (hasSelections) s"$operationName.${name.capitalize}" else innerType(tpe)
    writeField(writer, name, tpe, typeName, "  ")
  }

  private def writeNestedField(
    writer: Writer,
    name: String,
    tpe: Type,
    typePrefix: String,
    hasSelections: Boolean
  ): Unit = {
    val typeName = if (hasSelections) typePrefix + name.capitalize else innerType(tpe)
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
    writeNameAndType(writer, variable.name, variable.variableType, innerType(variable.variableType))
    variable.defaultValue.foreach { value =>
      writer.write(" = ")
      // TODO: Probably need to convert this to Scala.
      writer.write(value.toInputString)
    }
  }

  private def writeInputFieldParameter(writer: Writer, name: String, tpe: Type): Unit = {
    writer.write("    ")
    writeNameAndType(writer, name, tpe, innerType(tpe))
    if (tpe.nullable) {
      writer.write(" = null")
    }
  }

  private def writeNameAndType(writer: Writer, name: String, tpe: Type, typeName: String): Unit = {
    writer.write(name)
    writer.write(": ")
    if (tpe.isInstanceOf[Type.ListType]) writer.write("js.Array[")
    writer.write(convertType(typeName))
    if (tpe.isInstanceOf[Type.ListType]) writer.write("]")
    if (tpe.nullable) {
      writer.write(" | Null")
    }
  }

  // TODO: Don't do this. It only works if there is a single input object.
  private def operationSingleInputObjectFields(operation: OperationDefinition) = {
    operation.variableDefinitions match {
      case variable :: Nil =>
        schema.inputObjectTypes.get(innerType(variable.variableType)) match {
          case Some(_) if variable.defaultValue.nonEmpty =>
            // TODO: We could support this by providing a default object somewhere
            throw new UnsupportedOperationException("A single input object variable cannot have a default value.")
          case Some(obj) => obj.fields
          case None      => Nil
        }
      case _ => Nil
    }
  }

  private def getFieldDefinition(
    selectionObject: String,
    fieldLookup: Map[String, FieldDefinition]
  ): String => Option[FieldDefinition] =
    getFieldDefinition { fieldName =>
      fieldLookup.getOrElse(
        fieldName,
        throw new IllegalArgumentException(s"Type $selectionObject does not define field $fieldName.")
      )
    }

  private def getFieldDefinition(f: String => FieldDefinition)(fieldName: String): Option[FieldDefinition] =
    if (isMetaField(fieldName)) None
    else Some(f(fieldName))

  private def getFieldDefinitionTypeDefinition(
    typeName: String,
    fields: Map[String, FieldDefinition]
  ): String => Option[ObjectOrInterfaceTypeDefinition] =
    getFieldDefinition(typeName, fields)(_).map(fieldDefinitionTypeDefinition)

  private def getFieldDefinitionTypeDefinition(
    f: String => FieldDefinition
  )(fieldName: String): Option[ObjectOrInterfaceTypeDefinition] =
    getFieldDefinition(f)(fieldName).map(fieldDefinitionTypeDefinition)

  private def fieldDefinitionTypeDefinition(field: FieldDefinition): ObjectOrInterfaceTypeDefinition =
    schema.objectOrInterfaceType(innerType(field.ofType))

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

  private def nonMetaFieldSelections(selections: List[Selection]): List[Selection.Field] =
    selections.collect { case field: Selection.Field if !isMetaField(field.name) => field }

  private def inlineFragmentSelections(selections: List[Selection]): List[Selection.InlineFragment] =
    selections.collect { case inline: Selection.InlineFragment => inline }

  private def hasTypeName(selections: List[Selection]) =
    selections.exists {
      case field: Selection.Field => field.name == "__typename"
      case _                      => false
    }

  private def isMetaField(name: String) =
    name.startsWith("__")

  private def operationFile(operation: OperationDefinition) = {
    val name = getOperationName(operation)
    val file = outputDir.getAbsoluteFile / s"$name.scala"
    if (file.exists()) {
      if (outputs.contains(file))
        throw new IllegalArgumentException(
          s"File $file already exists. Ensure that you only have one operation named $name."
        )
      else file.delete()
    }
    file
  }

  private def fragmentFile(fragment: FragmentDefinition) = {
    val name = fragment.name
    val file = outputDir.getAbsoluteFile / s"$name.scala"
    if (file.exists()) {
      if (outputs.contains(file))
        throw new IllegalArgumentException(
          s"File $file already exists. Ensure that you only have one fragment named $name."
        )
      else file.delete()
    }
    file
  }

  private def getOperationName(operation: OperationDefinition) =
    operation.name.getOrElse(throw new UnsupportedOperationException("Anonymous queries are not not supported."))
}
