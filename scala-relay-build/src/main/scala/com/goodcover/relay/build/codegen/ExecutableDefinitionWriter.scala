package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.FieldDefinition
import caliban.parsing.adt.Type.{innerType, NamedType}
import caliban.parsing.adt.{Directive, Selection, Type}
import com.goodcover.relay.build.GraphQLSchema
import com.goodcover.relay.build.GraphQLSchema.FieldTypeDefinition
import com.goodcover.relay.build.GraphQLText.appendSelectionText
import com.goodcover.relay.build.codegen.ExecutableDefinitionWriter.idFieldDefinition
import com.goodcover.relay.build.codegen.Fields.{isID, isMetaField}
import com.goodcover.relay.build.codegen.Selections.{hasTypeName, inlineFragmentSelections, selectableFieldSelections}

import java.io.Writer
import scala.meta.Term
import scala.meta.XtensionSyntax

abstract class ExecutableDefinitionWriter(
  writer: Writer,
  // TODO: GC-3158 - Remove this.
  documentText: String,
  schema: GraphQLSchema,
  typeConverter: TypeConverter
) extends DefinitionWriter(writer) {

  def write(): Unit

  override protected def writeImports(): Unit = {
    writer.write("import _root_.scala.scalajs.js\n")
    writer.write("import _root_.scala.scalajs.js.|\n")
    writer.write("import _root_.scala.scalajs.js.annotation.JSImport\n\n")
  }

  // TODO: GC-3158 - Use DocumentRenderer instead.
  protected def writeDefinitionText(): Unit =
    appendSelectionText(documentText, containsStartOfDefinition)(writeDocumentTextLine)

  protected def containsStartOfDefinition(line: String): Boolean

  private def writeDocumentTextLine(line: String): Unit = {
    writer.write(line.replace("*/", "*\\/"))
    val last = line.lastOption
    if (!last.contains('\n') && !last.contains('\f')) {
      writer.write('\n')
    }
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

  private def writeNestedTrait(
    fullName: String,
    selections: List[Selection],
    subFieldLookup: Map[String, FieldDefinition],
    selectionObject: String,
    parentTraits: Seq[String]
  ): Unit = {
    val subFieldDefinition = getFieldDefinition(selectionObject, subFieldLookup)(_)
    writeSelectionTrait(fullName, selections, "  ", parentTraits) { subField =>
      subFieldDefinition(subField.name).foreach { definition =>
        writeNestedField(
          subField.alias.getOrElse(subField.name),
          definition.ofType,
          fullName,
          hasSelections = subField.selectionSet.nonEmpty,
          definition.directives
        )
      }
    }
  }

  private def writeSelectionTrait(name: String, selections: List[Selection], indent: String, parentTraits: Seq[String])(
    writeField: Selection.Field => Unit
  ): Unit = {
    val tname = meta.Type.Name(name)
    val parents =
      if (hasTypeName(selections)) parentTraits :+ s"_root_.com.goodcover.relay.Introspectable[${tname.syntax}]"
      else parentTraits
    val fieldSelections = selectableFieldSelections(selections)
    scalaWriter.writeTrait(tname, parents, fieldSelections, jsNative = true, indent)(writeField)
  }

  private def writeNestedSelection(
    selection: Selection,
    typeDefinition: FieldTypeDefinition,
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
            writeNestedSelection(subField, definition, fullName, outerObjectName, fullName)
          }
        }
      case inline: Selection.InlineFragment =>
        if (inline.selectionSet.nonEmpty) {
          val definition =
            inline.typeCondition.map(tpe => schema.fieldType(tpe.name)).getOrElse(typeDefinition)
          writeNestedSelection(inline, definition, fullName, outerObjectName, fullName)
        }
      case _: Selection.FragmentSpread =>
        () // Fragment spreads are handled as implicit conversions.
    }
    selection match {
      case field: Selection.Field =>
        val parentTraits = Directives.getExtends(field.directives).toList
        writeNestedTrait(fullName, field.selectionSet, subFieldLookup, typeDefinition.name, parentTraits)
        writeNestedInlineCompanionObject(fullName, field.selectionSet, outerObjectName)
      case inline: Selection.InlineFragment =>
        // FIXME: If there are multiple inline fragments without a type condition we will generate conflicting members.
        val selectionObject = inline.typeCondition.fold(typeDefinition.name)(_.name)
        writeNestedTrait(fullName, inline.selectionSet, subFieldLookup, selectionObject, Seq(inlineParent))
        writeNestedInlineCompanionObject(fullName, inline.selectionSet, outerObjectName)
      case _: Selection.FragmentSpread =>
        () // Fragment spreads are handled as implicit conversions.
    }
  }

  private def writeNestedInlineCompanionObject(
    fullName: String,
    selections: List[Selection],
    outerObjectName: String
  ): Unit = {
    val inlines = inlineFragmentSelections(selections)
    if (inlines.nonEmpty) {
      writer.write("  object ")
      writer.write(fullName)
      writer.write(" {\n")
      writeNestedTypeNameObject(Some(fullName), selections, outerObjectName, "    ", compact = true)
      writer.write("  }\n\n")
    }
  }

  protected def writeNestedTypeNameObject(
    innerObjectName: Option[String],
    selections: List[Selection],
    outerObjectName: String,
    indent: String,
    compact: Boolean
  ): Unit = {
    val inlines = inlineFragmentSelections(selections)
    if (inlines.nonEmpty) {
      writer.write(indent)
      writer.write("object __typename {\n")
      inlines.foreach { inline =>
        // FIXME: If there are multiple inline fragments without a type condition we will generate conflicting members.
        val name = inline.typeCondition.fold(innerObjectName.getOrElse(outerObjectName))(_.name)
        writeTypeName(name, s"$outerObjectName.${innerObjectName.getOrElse("")}$name", indent + "  ")
        // TODO: This is pretty silly. I don't know why relay compiler outputs this for TypeScript.
        //  If it is never going to be this then why do they have a type for it?
        //  Check this again once we upgrade.
      }
      writeTypeName("`%other`", innerObjectName.fold(outerObjectName)(n => s"$outerObjectName.$n"), indent + "  ")
      writer.write(indent)
      writer.write("}\n")
      if (!compact) writer.write('\n')
    }
  }

  private def writeTypeName(name: String, typeName: String, indent: String): Unit = {
    writer.write(indent)
    writer.write("@js.native sealed trait ")
    writer.write(name)
    writer.write(" extends _root_.com.goodcover.relay.Introspectable.TypeName[")
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

  private def writeNestedField(
    name: String,
    tpe: Type,
    typePrefix: String,
    hasSelections: Boolean,
    fieldDefinitionDirectives: List[Directive]
  ): Unit = {
    val typeName  = if (hasSelections) typePrefix + name.capitalize else innerType(tpe)
    val scalaType = typeConverter.convertToScalaType(tpe, typeName, fieldDefinitionDirectives, fullyQualified = false)
    scalaWriter.writeField(Term.Name(name), scalaType, None, "    ")
  }

  protected def writeFragmentImplicits(
    name: String,
    selections: List[Selection],
    outerObjectName: String,
    outer: Boolean = true
  ): Unit = {
    val typePrefix = if (outer) "" else name
    selections.foreach {
      case field: Selection.Field =>
        val nextName = typePrefix + field.alias.getOrElse(field.name).capitalize
        writeFragmentImplicits(nextName, field.selectionSet, outerObjectName, outer = false)
      case spread: Selection.FragmentSpread =>
        writeImplicitCastToFragmentRef(name, spread)
      case inline: Selection.InlineFragment =>
        val nextName = typePrefix + inline.typeCondition.fold(name)(_.name)
        writeFragmentImplicits(nextName, inline.selectionSet, outerObjectName, outer = false)
    }
    val inlines = inlineFragmentSelections(selections)
    writeInlineFragmentOps(name, inlines, typePrefix)
  }

  private def writeImplicitCastToFragmentRef(name: String, spread: Selection.FragmentSpread): Unit = {
    writer.write("  implicit class ")
    writer.write(name)
    writer.write('2')
    writer.write(spread.name)
    writer.write("Ref(f: ")
    writer.write(name)
    writer.write(") extends _root_.com.goodcover.relay.CastToFragmentRef[")
    writer.write(name)
    writer.write(", ")
    writer.write(spread.name)
    writer.write("](f) {\n")
    writer.write("    def to")
    writer.write(spread.name)
    writer.write(": _root_.com.goodcover.relay.FragmentRef[")
    writer.write(spread.name)
    writer.write("] = castToRef\n")
    writer.write("  }\n\n")
  }

  private def writeInlineFragmentOps(
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
        writer.write("] = _root_.com.goodcover.relay.Introspectable.as(f, ")
        writer.write(name)
        writer.write(".__typename.")
        writer.write(to)
        writer.write(")\n")
      }
      writer.write("  }\n\n")
    }
  }

  protected def writeGeneratedMapping(writer: Writer, name: String): Unit = {
    writer.write("""  @js.native
                   |  @JSImport("__generated__/""".stripMargin)
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
    if (isID(fieldName)) Some(idFieldDefinition)
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
}

object ExecutableDefinitionWriter {

  private val idFieldDefinition =
    FieldDefinition(None, "__id", Nil, NamedType("ID", nonNull = true), Nil)
}
