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
  val input: TestSubscriptionFeedbackLikeInput
}

trait TestSubscriptionFeedbackLikeInput extends js.Object {
  val clientMutationId: String | Null
  val feedbackId: String | Null
}

object TestSubscriptionFeedbackLikeInput {
  def apply(
    clientMutationId: String | Null = null,
    feedbackId: String | Null = null
  ): TestSubscriptionFeedbackLikeInput =
    js.Dynamic.literal(
      "clientMutationId" -> clientMutationId.asInstanceOf[js.Any],
      "feedbackId" -> feedbackId.asInstanceOf[js.Any]
    ).asInstanceOf[TestSubscriptionFeedbackLikeInput]
}

@js.native
trait TestSubscription extends js.Object {
  val feedbackLikeSubscribe: TestSubscription.FeedbackLikeSubscribe | Null
}

object TestSubscription extends _root_.relay.gql.SubscriptionTaggedNode[TestSubscriptionInput, TestSubscription] {
  type Ctor[T] = T

  @js.native
  trait FeedbackLikeSubscribe extends js.Object {
    val clientSubscriptionId: String | Null
  }

  def newInput(
    input: TestSubscriptionFeedbackLikeInput
  ): TestSubscriptionInput =
    js.Dynamic.literal(
      "input" -> input.asInstanceOf[js.Any]
    ).asInstanceOf[TestSubscriptionInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("./__generated__/TestSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
