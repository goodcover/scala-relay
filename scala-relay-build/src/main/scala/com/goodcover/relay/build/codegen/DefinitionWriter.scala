package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.FieldDefinition
import caliban.parsing.adt.Type.{innerType, NamedType}
import caliban.parsing.adt.{Directive, Selection, Type}
import com.goodcover.relay.build.GraphQLSchema
import com.goodcover.relay.build.GraphQLSchema.FieldTypeDefinition
import com.goodcover.relay.build.codegen.Fields.{isID, isMetaField}
import com.goodcover.relay.build.codegen.Selections._

import java.io.Writer
import scala.meta.Term

abstract class DefinitionWriter(writer: Writer) {

  protected val scalaWriter = new ScalaWriter(writer)

  def write(): Unit

  protected def writeImports(): Unit = {
    writer.write("import _root_.scala.scalajs.js\n")
    writer.write("import _root_.scala.scalajs.js.|\n")
    writer.write("import _root_.scala.scalajs.js.annotation.JSImport\n\n")
  }

  protected def writeHeader(): Unit = {
    writer.write("package relay.generated\n\n")
    writeImports()
  }

  protected def writeFooter(): Unit = {
    // Optional footer content
  }
}

abstract class ExecutableDefinitionWriter(
  writer: Writer,
  // TODO: GC-3158 - Remove this.
  documentText: String,
  schema: GraphQLSchema,
  typeConverter: TypeConverter
) extends DefinitionWriter(writer) {

  def write(): Unit

  protected def definitionName: String

  protected def writePreamble(): Unit = {
    writeHeader()
  }

  // TODO: Can we cleanup this field lookup stuff?
  protected def writeDefinitionTrait(selections: List[Selection], selectionField: FieldLookup): Unit = {
    writeSelectionTrait(definitionName, selections, "", Seq.empty) { field =>
      selectionField(field.name).foreach { definition =>
        writeDefinitionField(
          field.alias.getOrElse(field.name),
          definition.ofType,
          hasSelections = field.selectionSet.nonEmpty,
          definition.directives
        )
      }
    }
  }

  private def writeDefinitionField(
    name: String,
    tpe: Type,
    hasSelections: Boolean,
    fieldDefinitionDirectives: List[Directive]
  ): Unit = {
    // TODO: This typeName stuff is weird.
    val typeName  = if (hasSelections) s"$definitionName.${name.capitalize}" else innerType(tpe)
    val scalaType = typeConverter.convertToScalaType(tpe, typeName, fieldDefinitionDirectives, fullyQualified = false)
    scalaWriter.writeField(Term.Name(name), scalaType, None, "  ")
  }

  protected def writeSelectionTrait(
    traitName: String,
    selections: List[Selection],
    indent: String,
    parentTraits: Seq[String]
  )(writeField: Selection.Field => Unit): Unit = {
    val fields = selectableFieldSelections(selections)
    scalaWriter.writeTrait(
      tname = scala.meta.Type.Name(traitName),
      parentTraits = parentTraits,
      fields = fields,
      jsNative = true,
      indent = indent
    )(writeField)
  }

  // TODO: This stuff is weird.

  protected def getFieldDefinition(
    selectionObject: String,
    fieldLookup: Map[String, FieldDefinition]
  ): String => Option[FieldDefinition] =
    getFieldDefinition { fieldName =>
      fieldLookup.getOrElse(
        fieldName,
        throw new IllegalArgumentException(s"Type $selectionObject does not define field $fieldName.")
      )
    }

  protected def getFieldDefinition(f: String => FieldDefinition)(fieldName: String): Option[FieldDefinition] =
    if (isID(fieldName)) Some(ExecutableDefinitionWriter.idFieldDefinition)
    else if (isMetaField(fieldName)) None
    else Some(f(fieldName))

  protected def getFieldDefinitionTypeDefinition(
    typeName: String,
    fields: Map[String, FieldDefinition]
  ): String => Option[FieldTypeDefinition] =
    getFieldDefinition(typeName, fields)(_).map(fieldDefinitionTypeDefinition)

  protected def getFieldDefinitionTypeDefinition(
    f: String => FieldDefinition
  )(fieldName: String): Option[FieldTypeDefinition] =
    getFieldDefinition(f)(fieldName).map(fieldDefinitionTypeDefinition)

  private def fieldDefinitionTypeDefinition(field: FieldDefinition): FieldTypeDefinition =
    schema.fieldType(innerType(field.ofType))

  protected def writeNestedTraits(
    enclosingType: String,
    selections: List[Selection],
    fieldTypeDefinition: FieldTypeLookup,
    outerObjectName: String
  ): Unit = {
    selections.foreach {
      case field: Selection.Field =>
        if (field.selectionSet.nonEmpty) {
          fieldTypeDefinition(field.name).foreach { definition =>
            writeNestedSelection(field, definition, "", outerObjectName, outerObjectName)
          }
        }
      case inline: Selection.InlineFragment =>
        // Inline fragments may also be used to apply a directive to a group of fields. If the TypeCondition is omitted,
        // an inline fragment is considered to be of the same type as the enclosing context.
        // nonNull because we can only get an instance of this via the enclosing type which must exist first.
        val typeName   = inline.typeCondition.fold(enclosingType)(_.name)
        val definition = schema.fieldType(typeName)
        writeNestedSelection(inline, definition, "", outerObjectName, outerObjectName)
      case _: Selection.FragmentSpread => () // Fragment spreads are handled as implicit conversions.
    }
  }

  private def writeNestedSelection(
    selection: Selection,
    typeDefinition: FieldTypeDefinition,
    enclosingFullName: String,
    outerObjectName: String,
    selectionObject: String
  ): Unit = {
    val subSelections = selection match {
      case field: Selection.Field           => field.selectionSet
      case inline: Selection.InlineFragment => inline.selectionSet
      case _: Selection.FragmentSpread      => Nil // Unused.
    }
    val fullName = selection match {
      case field: Selection.Field =>
        val fieldName = field.alias.getOrElse(field.name).capitalize
        if (enclosingFullName.isEmpty) fieldName else s"$enclosingFullName.$fieldName"
      case inline: Selection.InlineFragment =>
        val typeName = inline.typeCondition.fold("Unknown")(_.name)
        if (enclosingFullName.isEmpty) typeName else s"$enclosingFullName.$typeName"
      case _: Selection.FragmentSpread => enclosingFullName // Unused.
    }

    // Write the nested trait
    writeNestedTrait(fullName, subSelections, typeDefinition.fields.map(f => f.name -> f).toMap, selectionObject)

    // Recursively write nested traits for sub-selections
    subSelections.foreach {
      case subField: Selection.Field =>
        if (subField.selectionSet.nonEmpty) {
          getFieldDefinitionTypeDefinition(typeDefinition.name, typeDefinition.fields.map(f => f.name -> f).toMap)(
            subField.name
          ).foreach { definition =>
            writeNestedSelection(subField, definition, fullName, outerObjectName, fullName)
          }
        }
      case inline: Selection.InlineFragment =>
        if (inline.selectionSet.nonEmpty) {
          val typeName   = inline.typeCondition.fold(typeDefinition.name)(_.name)
          val definition = schema.fieldType(typeName)
          writeNestedSelection(inline, definition, fullName, outerObjectName, fullName)
        }
      case _: Selection.FragmentSpread => () // Fragment spreads are handled as implicit conversions.
    }
  }

  private def writeNestedTrait(
    fullName: String,
    selections: List[Selection],
    subFieldLookup: Map[String, FieldDefinition],
    selectionObject: String
  ): Unit = {
    val fields = selectableFieldSelections(selections)
    if (fields.nonEmpty) {
      writer.write(s"object ${fullName.split('.').last} {\n")

      scalaWriter.writeTrait(
        tname = scala.meta.Type.Name(fullName.split('.').last),
        parentTraits = Seq.empty,
        fields = fields,
        jsNative = true,
        indent = "  "
      ) { field =>
        getFieldDefinition(selectionObject, subFieldLookup)(field.name).foreach { definition =>
          writeDefinitionField(
            field.alias.getOrElse(field.name),
            definition.ofType,
            hasSelections = field.selectionSet.nonEmpty,
            definition.directives
          )
        }
      }

      writer.write("}\n\n")
    }
  }

  protected def writeNestedTypeNameObject(
    typeCondition: Option[String],
    selections: List[Selection],
    outerObjectName: String,
    indent: String,
    compact: Boolean
  ): Unit = {
    // Placeholder implementation - would need to implement proper type name object generation
    // This is complex logic that handles __typename fields
  }

  protected def writeFragmentImplicits(
    name: String,
    selections: List[Selection],
    outerObjectName: String
  ): Unit = {
    // Placeholder implementation - would need to implement fragment implicit conversions
    // This handles fragment spread implicit conversions
  }

  protected def writeGeneratedMapping(writer: Writer, name: String): Unit = {
    writer.write("  val node: String = \"\"\"\n")
    writer.write("    # GraphQL operation placeholder\n")
    writer.write("  \"\"\"\n")
    writer.write("}\n")
  }
}

object ExecutableDefinitionWriter {

  private val idFieldDefinition =
    FieldDefinition(None, "__id", Nil, NamedType("ID", nonNull = true), Nil)
}
