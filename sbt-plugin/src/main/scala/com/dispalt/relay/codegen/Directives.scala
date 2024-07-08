package com.dispalt.relay.codegen

import caliban.Value.StringValue
import caliban.parsing.adt.Directive

private[relay] object Directives {

  //private final case class ScalaJSDirective(useNulls: Boolean, `extends`: String, typeCls: String, clientType: String)

  def clientType(directives: List[Directive]): Option[String] =
    directives.find(_.name == "scalajs").flatMap { directive =>
      directive.arguments.get("clientType").map {
        case StringValue(typeArg) => typeArg
        case _                    => throw new IllegalArgumentException("Invalid scalajs directive. clientType must be a String.")
      }
    }

  def inline(directives: List[Directive]): Boolean =
    directives.exists(_.name == "inline")
}
