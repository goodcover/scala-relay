package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

/*
input Nested {
  input: Input!
}
 */

trait Nested extends js.Object {
  val input: Input
}

object Nested {
  def apply(
    input: Input
  ): Nested =
    js.Dynamic
      .literal(
        input = input
      )
      .asInstanceOf[Nested]
}
