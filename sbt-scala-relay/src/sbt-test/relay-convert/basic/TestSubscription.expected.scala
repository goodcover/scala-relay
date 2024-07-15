package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestSubscription($input: FeedbackLikeInput!) {
    feedbackLikeSubscribe(input: $input) {
        clientSubscriptionId
    }
}
*/

trait TestSubscriptionInput extends js.Object {
  val input: FeedbackLikeInput
}

object TestSubscriptionInput {
  def apply(
    input: FeedbackLikeInput
  ): TestSubscriptionInput =
    js.Dynamic.literal(
      "input" -> input.asInstanceOf[js.Any]
    ).asInstanceOf[TestSubscriptionInput]
}

@js.native
trait TestSubscription extends js.Object {
  val feedbackLikeSubscribe: TestSubscription.FeedbackLikeSubscribe | Null
}

object TestSubscription extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestSubscriptionInput, TestSubscription] {
  type Ctor[T] = T

  @js.native
  trait FeedbackLikeSubscribe extends js.Object {
    val clientSubscriptionId: String | Null
  }

  def newInput(
    input: _root_.relay.generated.FeedbackLikeInput
  ): _root_.relay.generated.TestSubscriptionInput =
    _root_.relay.generated.TestSubscriptionInput(
      input
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
