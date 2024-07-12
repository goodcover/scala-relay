package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestNestedObjectVariableMutation($input: CommentCreateInput) {
    commentCreate(input: $input) {
        clientMutationId
    }
}
*/

trait TestNestedObjectVariableMutationInput extends js.Object {
  val clientMutationId: String | Null
  val feedbackId: String | Null
  val feedback: TestNestedObjectVariableMutationCommentfeedbackFeedback | Null
}

trait TestNestedObjectVariableMutationCommentfeedbackFeedback extends js.Object {
  val comment: TestNestedObjectVariableMutationFeedbackcommentComment | Null
}

object TestNestedObjectVariableMutationCommentfeedbackFeedback {
  def apply(
    comment: TestNestedObjectVariableMutationFeedbackcommentComment | Null = null
  ): TestNestedObjectVariableMutationCommentfeedbackFeedback =
    js.Dynamic.literal(
      "comment" -> comment.asInstanceOf[js.Any]
    ).asInstanceOf[TestNestedObjectVariableMutationCommentfeedbackFeedback]
}

trait TestNestedObjectVariableMutationFeedbackcommentComment extends js.Object {
  val feedback: TestNestedObjectVariableMutationCommentfeedbackFeedback | Null
}

object TestNestedObjectVariableMutationFeedbackcommentComment {
  def apply(
    feedback: TestNestedObjectVariableMutationCommentfeedbackFeedback | Null = null
  ): TestNestedObjectVariableMutationFeedbackcommentComment =
    js.Dynamic.literal(
      "feedback" -> feedback.asInstanceOf[js.Any]
    ).asInstanceOf[TestNestedObjectVariableMutationFeedbackcommentComment]
}

@js.native
trait TestNestedObjectVariableMutation extends js.Object {
  val commentCreate: TestNestedObjectVariableMutation.CommentCreate | Null
}

object TestNestedObjectVariableMutation extends _root_.relay.gql.MutationTaggedNode[TestNestedObjectVariableMutationInput, TestNestedObjectVariableMutation] {
  type Ctor[T] = T

  @js.native
  trait CommentCreate extends js.Object {
    val clientMutationId: String | Null
  }

  def newInput(
    clientMutationId: String | Null = null,
    feedbackId: String | Null = null,
    feedback: TestNestedObjectVariableMutationCommentfeedbackFeedback | Null = null
  ): TestNestedObjectVariableMutationInput =
    js.Dynamic.literal(
      "clientMutationId" -> clientMutationId.asInstanceOf[js.Any],
      "feedbackId" -> feedbackId.asInstanceOf[js.Any],
      "feedback" -> feedback.asInstanceOf[js.Any]
    ).asInstanceOf[TestNestedObjectVariableMutationInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestNestedObjectVariableMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
