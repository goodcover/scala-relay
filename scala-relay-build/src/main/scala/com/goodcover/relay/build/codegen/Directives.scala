package com.goodcover.relay.build.codegen

import caliban.InputValue
import caliban.InputValue.ListValue
import caliban.Value.{ BooleanValue, StringValue }
import caliban.parsing.adt.Directive

private[relay] object Directives {

  // TODO: Parse directives.
  final case class Refetchable(queryName: String, directives: List[String])

  // private final case class ScalaJSDirective(useNulls: Boolean, `extends`: String, typeCls: String, clientType: String)

  def isInline(fragmentDirectives: List[Directive]): Boolean =
    fragmentDirectives.exists(_.name == "inline")

  def isRefetchable(fragmentDirectives: List[Directive]): Boolean =
    fragmentDirectives.exists(_.name == "refetchable")

  def isPlural(fragmentDirectives: List[Directive]): Boolean =
    getBooleanDirectiveArg(fragmentDirectives, "relay", "plural").getOrElse(false)

  def getClientType(fieldDefinitionDirectives: List[Directive]): Option[String] =
    getStringDirectiveArg(fieldDefinitionDirectives, "scalajs", "clientType")

  // TODO: It ought to be possible to put this on input types too once they are shared.
  def getExtends(fieldSelectionDirectives: List[Directive]): Option[String] =
    getStringDirectiveArg(fieldSelectionDirectives, "scalajs", "extends")

  def getRefetchable(fragmentDirectives: List[Directive]): Option[Refetchable] =
    fragmentDirectives.find(_.name == "refetchable").map { directive =>
      val queryName  = stringValue(
        directive.arguments.getOrElse(
          "queryName",
          throw new IllegalArgumentException("Invalid refetchable directive. queryName is required.")
        ),
        "refetchable",
        "queryName"
      )
      val directives =
        directive.arguments.get("directives").map(stringValues(_, "refetchable", "queryName")).getOrElse(Nil)
      Refetchable(queryName, directives)
    }

  private def getBooleanDirectiveArg(
    directives: List[Directive],
    directiveName: String,
    argName: String
  ): Option[Boolean] =
    getDirectiveArg(directives, directiveName, argName).map(booleanValue(_, directiveName, argName))

  private def getStringDirectiveArg(
    directives: List[Directive],
    directiveName: String,
    argName: String
  ): Option[String] =
    getDirectiveArg(directives, directiveName, argName).map(stringValue(_, directiveName, argName))

  private def getDirectiveArg(directives: List[Directive], directiveName: String, argName: String): Option[InputValue] =
    for {
      directive <- directives.find(_.name == directiveName)
      arg       <- directive.arguments.get(argName)
    } yield arg

  private def booleanValue(value: InputValue, directiveName: String, argName: String): Boolean =
    value match {
      case BooleanValue(b) => b
      case _               => throw new IllegalArgumentException(s"Invalid $directiveName directive. $argName must be a Boolean.")
    }

  private def stringValue(value: InputValue, directiveName: String, argName: String): String =
    value match {
      case StringValue(s) => s
      case _              => throw new IllegalArgumentException(s"Invalid $directiveName directive. $argName must be a String.")
    }

  private def stringValues(value: InputValue, directiveName: String, argName: String): List[String] =
    value match {
      case ListValue(values) =>
        values.map {
          case StringValue(s) => s
          case _              =>
            throw new IllegalArgumentException(s"Invalid $directiveName directive. $argName must be a [String].")
        }
      case _                 => throw new IllegalArgumentException(s"Invalid $directiveName directive. $argName must be a [String].")
    }
}
