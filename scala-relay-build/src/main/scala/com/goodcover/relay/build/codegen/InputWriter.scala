package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.InputObjectTypeDefinition
import caliban.parsing.adt.Document
import com.goodcover.relay.build.GraphQLSchema
import com.goodcover.relay.build.codegen.ScalaWriter.Parameter

import java.io.Writer
import scala.meta.{Term, Type}
import caliban.parsing.adt.{Type => GQLType}

class InputWriter(
  writer: Writer,
  input: InputObjectTypeDefinition,
  document: Document,
  schema: GraphQLSchema,
  typeConverter: TypeConverter
) extends DefinitionWriter(writer) {

  override def write(): Unit = {
    writeHeader()
    writeInputType()
    writeFooter()
  }

  private def writeInputType(): Unit = {
    val typeName = Type.Name(input.name)
    val parameters = input.fields.map { field =>
      val fieldName = Term.Name(field.name)
      val typeName = extractTypeName(field.ofType)
      val scalaType = typeConverter.convertToScalaType(
        field.ofType,
        typeName,
        field.directives,
        fullyQualified = false
      )
      val initializer = if (field.ofType.nonNull) None else Some("js.undefined")
      Parameter(fieldName, scalaType, initializer)
    }

    // Write the trait
    scalaWriter.writeTrait(
      tname = typeName,
      parentTraits = Seq.empty,
      fields = parameters,
      jsNative = true,
      indent = ""
    ) { parameter =>
      scalaWriter.writeField(
        ename = parameter.ename,
        scalaType = parameter.scalaType,
        initializer = parameter.initializer,
        indent = "  "
      )
    }

    // Write the companion object with apply method
    writer.write("object ")
    writer.write(input.name)
    writer.write(" {\n")

    scalaWriter.writeJsLiteralApplyMethod(
      parameters = parameters,
      typeName = input.name,
      compact = false,
      indent = "  "
    )

    writer.write("}\n")
  }

  private def extractTypeName(gqlType: GQLType): String = gqlType match {
    case GQLType.NamedType(name, _) => name
    case GQLType.ListType(ofType, _) => extractTypeName(ofType)
  }
}
