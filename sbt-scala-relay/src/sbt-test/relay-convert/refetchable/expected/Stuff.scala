package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

/*
input Stuff {
  junk: String
}
*/

trait Stuff extends js.Object {
  val junk: js.UndefOr[String | Null]
}

object Stuff {
  def apply(
    junk: js.UndefOr[String | Null] = js.undefined
  ): Stuff =
    js.Dynamic.literal(
      junk = junk
    ).asInstanceOf[Stuff]
}
