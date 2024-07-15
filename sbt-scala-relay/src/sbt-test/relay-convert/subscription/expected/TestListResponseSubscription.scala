package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestListResponseSubscription($a: String!) {
    listResponse(a: $a)
}
*/

trait TestListResponseSubscriptionInput extends js.Object {
  val a: String
}

object TestListResponseSubscriptionInput {
  def apply(
    a: String
  ): TestListResponseSubscriptionInput =
    js.Dynamic.literal(
      "a" -> a.asInstanceOf[js.Any]
    ).asInstanceOf[TestListResponseSubscriptionInput]
}

@js.native
trait TestListResponseSubscription extends js.Object {
  val listResponse: js.Array[String]
}

object TestListResponseSubscription extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestListResponseSubscriptionInput, TestListResponseSubscription] {
  type Ctor[T] = T

  def newInput(
    a: String
  ): _root_.relay.generated.TestListResponseSubscriptionInput =
    _root_.relay.generated.TestListResponseSubscriptionInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestListResponseSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
