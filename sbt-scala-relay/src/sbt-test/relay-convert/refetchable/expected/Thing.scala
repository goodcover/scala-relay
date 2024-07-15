package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

/*
input Thing {
  stuff: Stuff!
}
*/

trait Thing extends js.Object {
  val stuff: Stuff
}

object Thing {
  def apply(
    stuff: Stuff
  ): Thing =
    js.Dynamic.literal(
      "stuff" -> stuff.asInstanceOf[js.Any]
    ).asInstanceOf[Thing]
}
