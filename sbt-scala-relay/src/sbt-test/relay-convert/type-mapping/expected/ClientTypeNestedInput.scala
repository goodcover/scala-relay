package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

/*
input ClientTypeNestedInput {
  nested: Foo!
}
 */

trait ClientTypeNestedInput extends js.Object {
  val nested: Bar
}

object ClientTypeNestedInput {
  def apply(
    nested: Bar
  ): ClientTypeNestedInput =
    js.Dynamic
      .literal(
        nested = nested
      )
      .asInstanceOf[ClientTypeNestedInput]
}
