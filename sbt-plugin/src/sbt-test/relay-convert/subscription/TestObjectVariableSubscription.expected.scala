package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestObjectVariableSubscription($input: FeedbackLikeInput!) {
    feedbackLikeSubscribe(input: $input) {
        clientSubscriptionId
    }
}
*/

trait TestObjectVariableSubscriptionInput extends js.Object {
  val input: TestObjectVariableSubscriptionFeedbackLikeInput
}

trait TestObjectVariableSubscriptionFeedbackLikeInput extends js.Object {
  val clientMutationId: String | Null
  val feedbackId: String | Null
}

object TestObjectVariableSubscriptionFeedbackLikeInput {
  def apply(
    clientMutationId: String | Null = null,
    feedbackId: String | Null = null
  ): TestObjectVariableSubscriptionFeedbackLikeInput =
    js.Dynamic.literal(
      "clientMutationId" -> clientMutationId.asInstanceOf[js.Any],
      "feedbackId" -> feedbackId.asInstanceOf[js.Any]
    ).asInstanceOf[TestObjectVariableSubscriptionFeedbackLikeInput]
}

@js.native
trait TestObjectVariableSubscription extends js.Object {
  val feedbackLikeSubscribe: TestObjectVariableSubscription.FeedbackLikeSubscribe | Null
}

object TestObjectVariableSubscription extends _root_.relay.gql.SubscriptionTaggedNode[TestObjectVariableSubscriptionInput, TestObjectVariableSubscription] {
  type Ctor[T] = T

  @js.native
  trait FeedbackLikeSubscribe extends js.Object {
    val clientSubscriptionId: String | Null
  }

  def newInput(
    input: TestObjectVariableSubscriptionFeedbackLikeInput
  ): TestObjectVariableSubscriptionInput =
    js.Dynamic.literal(
      "input" -> input.asInstanceOf[js.Any]
    ).asInstanceOf[TestObjectVariableSubscriptionInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestObjectVariableSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
