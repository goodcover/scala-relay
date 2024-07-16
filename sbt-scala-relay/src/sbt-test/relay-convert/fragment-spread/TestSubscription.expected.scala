package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestSubscription($input: FeedbackLikeInput!) {
    feedbackLikeSubscribe(input: $input) {
        clientSubscriptionId
        ...Test_payload2
        feedback {
            ...Test_feedback
        }
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

object TestSubscription extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestSubscriptionInput, TestSubscription] {
  type Ctor[T] = T

  @js.native
  trait FeedbackLikeSubscribeFeedback extends js.Object

  @js.native
  trait FeedbackLikeSubscribe extends js.Object {
    val clientSubscriptionId: String | Null
    val feedback: FeedbackLikeSubscribeFeedback | Null
  }

  implicit class FeedbackLikeSubscribe2Test_payload2Ref(f: FeedbackLikeSubscribe) extends _root_.com.goodcover.relay.CastToFragmentRef[FeedbackLikeSubscribe, Test_payload2](f) {
    def toTest_payload2: _root_.com.goodcover.relay.FragmentRef[Test_payload2] = castToRef
  }

  implicit class FeedbackLikeSubscribeFeedback2Test_feedbackRef(f: FeedbackLikeSubscribeFeedback) extends _root_.com.goodcover.relay.CastToFragmentRef[FeedbackLikeSubscribeFeedback, Test_feedback](f) {
    def toTest_feedback: _root_.com.goodcover.relay.FragmentRef[Test_feedback] = castToRef
  }

  def newInput(
    input: TestSubscriptionFeedbackLikeInput
  ): TestSubscriptionInput =
    js.Dynamic.literal(
      "input" -> input.asInstanceOf[js.Any]
    ).asInstanceOf[TestSubscriptionInput]

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
