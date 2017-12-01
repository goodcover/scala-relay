package relay.runtime

import scala.scalajs.js

trait Edge[E] extends js.Object {
  val node: E
  val cursor: String
}

// Can't figure how to handle the variance here.
trait Connection[E <: Edge[_]] extends js.Object {
  val edges: js.Array[E]
}

trait SangriaInfo extends js.Object {
  val hasNextPage: Boolean
  val hasPreviousPage: Boolean
  val startCursor: NullOr[String]
  val endCursor: NullOr[String]
}
