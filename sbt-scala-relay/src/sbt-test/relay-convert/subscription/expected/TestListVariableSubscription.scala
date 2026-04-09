package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestListVariableSubscription($as: [String!]!) {
    listVariable(as: $as) {
        id
    }
}
 */

trait TestListVariableSubscriptionInput extends js.Object {
  val as: js.Array[String]
}

object TestListVariableSubscriptionInput {
  def apply(
    as: js.Array[String]
  ): TestListVariableSubscriptionInput =
    js.Dynamic
      .literal(
        as = as
      )
      .asInstanceOf[TestListVariableSubscriptionInput]
}

@js.native
trait TestListVariableSubscription extends js.Object {
  val listVariable: TestListVariableSubscription.ListVariable
}

object TestListVariableSubscription
    extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestListVariableSubscriptionInput, TestListVariableSubscription] {
  type Ctor[T] = T

  @js.native
  trait ListVariable extends js.Object {
    val id: String
  }

  def newInput(
    as: js.Array[String]
  ): _root_.relay.generated.TestListVariableSubscriptionInput =
    _root_.relay.generated.TestListVariableSubscriptionInput(
      as
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestListVariableSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
