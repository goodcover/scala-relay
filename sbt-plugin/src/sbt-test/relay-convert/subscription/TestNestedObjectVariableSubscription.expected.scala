package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestNestedObjectVariableSubscription($input: CommentCreateInput) {
    commentCreateSubscribe(input: $input) {
        clientMutationId
    }
}
*/

trait TestNestedObjectVariableSubscriptionInput extends js.Object {
  val clientMutationId: String | Null
  val feedbackId: String | Null
  val feedback: TestNestedObjectVariableSubscriptionCommentfeedbackFeedback | Null
}

trait TestNestedObjectVariableSubscriptionCommentfeedbackFeedback extends js.Object {
  val comment: TestNestedObjectVariableSubscriptionFeedbackcommentComment | Null
}

object TestNestedObjectVariableSubscriptionCommentfeedbackFeedback {
  def apply(
    comment: TestNestedObjectVariableSubscriptionFeedbackcommentComment | Null = null
  ): TestNestedObjectVariableSubscriptionCommentfeedbackFeedback =
    js.Dynamic.literal(
      "comment" -> comment.asInstanceOf[js.Any]
    ).asInstanceOf[TestNestedObjectVariableSubscriptionCommentfeedbackFeedback]
}

trait TestNestedObjectVariableSubscriptionFeedbackcommentComment extends js.Object {
  val feedback: TestNestedObjectVariableSubscriptionCommentfeedbackFeedback | Null
}

object TestNestedObjectVariableSubscriptionFeedbackcommentComment {
  def apply(
    feedback: TestNestedObjectVariableSubscriptionCommentfeedbackFeedback | Null = null
  ): TestNestedObjectVariableSubscriptionFeedbackcommentComment =
    js.Dynamic.literal(
      "feedback" -> feedback.asInstanceOf[js.Any]
    ).asInstanceOf[TestNestedObjectVariableSubscriptionFeedbackcommentComment]
}

@js.native
trait TestNestedObjectVariableSubscription extends js.Object {
  val commentCreateSubscribe: TestNestedObjectVariableSubscription.CommentCreateSubscribe | Null
}

object TestNestedObjectVariableSubscription extends _root_.relay.gql.SubscriptionTaggedNode[TestNestedObjectVariableSubscriptionInput, TestNestedObjectVariableSubscription] {
  type Ctor[T] = T

  @js.native
  trait CommentCreateSubscribe extends js.Object {
    val clientMutationId: String | Null
  }

  def newInput(
    clientMutationId: String | Null = null,
    feedbackId: String | Null = null,
    feedback: TestNestedObjectVariableSubscriptionCommentfeedbackFeedback | Null = null
  ): TestNestedObjectVariableSubscriptionInput =
    js.Dynamic.literal(
      "clientMutationId" -> clientMutationId.asInstanceOf[js.Any],
      "feedbackId" -> feedbackId.asInstanceOf[js.Any],
      "feedback" -> feedback.asInstanceOf[js.Any]
    ).asInstanceOf[TestNestedObjectVariableSubscriptionInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestNestedObjectVariableSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
