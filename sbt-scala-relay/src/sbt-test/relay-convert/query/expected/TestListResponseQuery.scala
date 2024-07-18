package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestListResponseQuery($a: String!) {
    listResponse(a: $a)
}
*/

trait TestListResponseQueryInput extends js.Object {
  val a: String
}

object TestListResponseQueryInput {
  def apply(
    a: String
  ): TestListResponseQueryInput =
    js.Dynamic.literal(
      a = a
    ).asInstanceOf[TestListResponseQueryInput]
}

@js.native
trait TestListResponseQuery extends js.Object {
  val listResponse: js.Array[String]
}

object TestListResponseQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestListResponseQueryInput, TestListResponseQuery] {
  type Ctor[T] = T

  def newInput(
    a: String
  ): _root_.relay.generated.TestListResponseQueryInput =
    _root_.relay.generated.TestListResponseQueryInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestListResponseQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
