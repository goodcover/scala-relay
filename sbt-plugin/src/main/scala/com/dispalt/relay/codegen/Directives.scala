package com.dispalt.relay.codegen

import caliban.InputValue
import caliban.Value.{BooleanValue, StringValue}
import caliban.parsing.adt.Directive

private[relay] object Directives {

  //private final case class ScalaJSDirective(useNulls: Boolean, `extends`: String, typeCls: String, clientType: String)

  def isInline(fragmentDirectives: List[Directive]): Boolean =
    fragmentDirectives.exists(_.name == "inline")

  def isPlural(fragmentDirectives: List[Directive]): Boolean =
    getDirectiveArg(fragmentDirectives, "relay", "plural").fold(false) {
      case BooleanValue(plural) => plural
      case _                    => throw new IllegalArgumentException("Invalid relay directive. plural must be a Boolean.")
    }

  def getClientType(fieldDefinitionDirectives: List[Directive]): Option[String] =
    getDirectiveArg(fieldDefinitionDirectives, "scalajs", "clientType") collect {
      case StringValue(typeArg) => typeArg
      case _                    => throw new IllegalArgumentException("Invalid scalajs directive. clientType must be a String.")
    }

  // TODO: It ought to be possible to put this on input types too once they are shared.
  def getExtends(fieldSelectionDirectives: List[Directive]): Option[String] =
    getDirectiveArg(fieldSelectionDirectives, "scalajs", "extends") collect {
      case StringValue(parentTrait) => parentTrait
      // TODO: This should ideally support multiple parents.
      case _                        => throw new IllegalArgumentException("Invalid scalajs directive. extends must be a String.")
    }

  private def getDirectiveArg(directives: List[Directive], directiveName: String, argName: String): Option[InputValue] =
    for {
      directive <- directives.find(_.name == directiveName)
      arg       <- directive.arguments.get(argName)
    } yield arg
}
