package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestOptionalPrimitiveResponseSubscription($a: String!) {
    optionalPrimitiveResponse(a: $a)
}
*/

trait TestOptionalPrimitiveResponseSubscriptionInput extends js.Object {
  val a: String
}

object TestOptionalPrimitiveResponseSubscriptionInput {
  def apply(
    a: String
  ): TestOptionalPrimitiveResponseSubscriptionInput =
    js.Dynamic.literal(
      a = a
    ).asInstanceOf[TestOptionalPrimitiveResponseSubscriptionInput]
}

@js.native
trait TestOptionalPrimitiveResponseSubscription extends js.Object {
  val optionalPrimitiveResponse: String | Null
}

object TestOptionalPrimitiveResponseSubscription extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestOptionalPrimitiveResponseSubscriptionInput, TestOptionalPrimitiveResponseSubscription] {
  type Ctor[T] = T

  def newInput(
    a: String
  ): _root_.relay.generated.TestOptionalPrimitiveResponseSubscriptionInput =
    _root_.relay.generated.TestOptionalPrimitiveResponseSubscriptionInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalPrimitiveResponseSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
