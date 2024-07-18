package com.goodcover.relay.codegen

import caliban.parsing.adt.{Directive, Type}
import com.goodcover.relay.GraphQLSchema
import com.goodcover.relay.codegen.TypeConverter.DefaultTypeMappings

class TypeConverter(schema: GraphQLSchema, typeMappings: Map[String, String]) {

  def convertToScalaType(gqlTypeName: String, fullyQualified: Boolean): String =
    typeMappings
      .get(gqlTypeName)
      .orElse(DefaultTypeMappings.get(gqlTypeName))
      // TODO: We ought to handle enum types better.
      .orElse(if (schema.enumTypes.contains(gqlTypeName)) Some("String") else None)
      .getOrElse(
        if (!fullyQualified || schema.scalarTypes.contains(gqlTypeName)) gqlTypeName
        else "_root_.relay.generated." + gqlTypeName
      )

  // TODO: This is confusing. Type already has the name. We should update that instead of passing it separately.
  def convertToScalaType(
    tpe: Type,
    gqlTypeName: String,
    fieldDefinitionDirectives: List[Directive],
    fullyQualified: Boolean
  ): String = {
    val builder = new StringBuilder()
    def loop(tpe: Type): Unit = {
      tpe match {
        case Type.NamedType(_, _) =>
          builder.append(convertToScalaType(gqlTypeName, fullyQualified))
          Directives.getClientType(fieldDefinitionDirectives).foreach { typeArg =>
            builder.append('[')
            builder.append(typeArg)
            builder.append(']')
          }
        case Type.ListType(ofType, _) =>
          builder.append("js.Array[")
          loop(ofType)
          builder.append(']')
      }
      if (!tpe.nonNull) {
        val _ = builder.append(" | Null")
      }
    }

    loop(tpe)
    builder.result()
  }
}

object TypeConverter {

  private val DefaultTypeMappings: Map[String, String] =
    Map( //
      "Int"     -> "Int",
      "Float"   -> "Double",
      "String"  -> "String",
      "Boolean" -> "Boolean",
      "ID"      -> "String"
    )
}
