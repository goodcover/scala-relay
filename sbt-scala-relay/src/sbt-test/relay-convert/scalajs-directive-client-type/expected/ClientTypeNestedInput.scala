package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

/*
input ClientTypeNestedInput {
  nested: String! @scalajs(clientType: "Nested")
}
*/

trait ClientTypeNestedInput extends js.Object {
  val nested: String[Nested]
}

object ClientTypeNestedInput {
  def apply(
    nested: String[Nested]
  ): ClientTypeNestedInput =
    js.Dynamic.literal(
      nested = nested
    ).asInstanceOf[ClientTypeNestedInput]
}
