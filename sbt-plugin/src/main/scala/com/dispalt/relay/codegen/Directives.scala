package com.dispalt.relay.codegen

import caliban.InputValue
import caliban.Value.{BooleanValue, StringValue}
import caliban.parsing.adt.Directive

private[relay] object Directives {

  //private final case class ScalaJSDirective(useNulls: Boolean, `extends`: String, typeCls: String, clientType: String)

  def getClientType(fieldDefinitionDirectives: List[Directive]): Option[String] =
    getDirectiveArg(fieldDefinitionDirectives, "scalajs", "clientType") collect {
      case StringValue(typeArg) => typeArg
      case _                    => throw new IllegalArgumentException("Invalid scalajs directive. clientType must be a String.")
    }

  def isInline(fragmentDirectives: List[Directive]): Boolean =
    fragmentDirectives.exists(_.name == "inline")

  def isPlural(fragmentDirectives: List[Directive]): Boolean =
    getDirectiveArg(fragmentDirectives, "relay", "plural").fold(false) {
      case BooleanValue(plural) => plural
      case _                    => throw new IllegalArgumentException("Invalid relay directive. plural must be a Boolean.")
    }

  private def getDirectiveArg(directives: List[Directive], directiveName: String, argName: String): Option[InputValue] =
    for {
      directive <- directives.find(_.name == directiveName)
      arg       <- directive.arguments.get(argName)
    } yield arg
}
