package relay.runtime

import scala.scalajs.js

trait Edge[E] extends js.Object {
  val node: E
  val cursor: String
}

trait Connection[E] extends js.Object {
  val edges: js.Array[Edge[E]]
}

trait SangriaInfo extends js.Object {
  val hasNextPage: Boolean
  val hasPreviousPage: Boolean
  val startCursor: NullOr[String]
  val endCursor: NullOr[String]
}
