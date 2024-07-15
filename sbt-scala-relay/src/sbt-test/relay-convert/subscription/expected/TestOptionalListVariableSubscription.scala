package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestOptionalListVariableSubscription($as: [String!]) {
    listVariable(as: $as) {
        id
    }
}
*/

trait TestOptionalListVariableSubscriptionInput extends js.Object {
  val as: js.Array[String] | Null
}

object TestOptionalListVariableSubscriptionInput {
  def apply(
    as: js.Array[String] | Null = null
  ): TestOptionalListVariableSubscriptionInput =
    js.Dynamic.literal(
      "as" -> as.asInstanceOf[js.Any]
    ).asInstanceOf[TestOptionalListVariableSubscriptionInput]
}

@js.native
trait TestOptionalListVariableSubscription extends js.Object {
  val listVariable: TestOptionalListVariableSubscription.ListVariable
}

object TestOptionalListVariableSubscription extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestOptionalListVariableSubscriptionInput, TestOptionalListVariableSubscription] {
  type Ctor[T] = T

  @js.native
  trait ListVariable extends js.Object {
    val id: String
  }

  def newInput(
    as: js.Array[String] | Null = null
  ): _root_.relay.generated.TestOptionalListVariableSubscriptionInput =
    _root_.relay.generated.TestOptionalListVariableSubscriptionInput(
      as
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalListVariableSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
