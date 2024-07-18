package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

/*
input Stuff {
  junk: String
}
*/

trait Stuff extends js.Object {
  val junk: String | Null
}

object Stuff {
  def apply(
    junk: String | Null = null
  ): Stuff =
    js.Dynamic.literal(
      junk = junk
    ).asInstanceOf[Stuff]
}
