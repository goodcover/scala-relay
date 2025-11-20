package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestPrimitiveResponseSubscription($a: String!) {
    primitiveResponse(a: $a)
}
 */

trait TestPrimitiveResponseSubscriptionInput extends js.Object {
  val a: String
}

object TestPrimitiveResponseSubscriptionInput {
  def apply(
    a: String
  ): TestPrimitiveResponseSubscriptionInput =
    js.Dynamic
      .literal(
        a = a
      )
      .asInstanceOf[TestPrimitiveResponseSubscriptionInput]
}

@js.native
trait TestPrimitiveResponseSubscription extends js.Object {
  val primitiveResponse: String
}

object TestPrimitiveResponseSubscription
    extends _root_.com.goodcover.relay.SubscriptionTaggedNode[
      TestPrimitiveResponseSubscriptionInput,
      TestPrimitiveResponseSubscription
    ] {
  type Ctor[T] = T

  def newInput(
    a: String
  ): _root_.relay.generated.TestPrimitiveResponseSubscriptionInput =
    _root_.relay.generated.TestPrimitiveResponseSubscriptionInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestPrimitiveResponseSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
