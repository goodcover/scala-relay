package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestObjectVariableSubscription($input: Input!) {
    objectVariable(input: $input) {
        id
    }
}
*/

trait TestObjectVariableSubscriptionInput extends js.Object {
  val input: Input
}

object TestObjectVariableSubscriptionInput {
  def apply(
    input: Input
  ): TestObjectVariableSubscriptionInput =
    js.Dynamic.literal(
      input = input
    ).asInstanceOf[TestObjectVariableSubscriptionInput]
}

@js.native
trait TestObjectVariableSubscription extends js.Object {
  val objectVariable: TestObjectVariableSubscription.ObjectVariable
}

object TestObjectVariableSubscription extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestObjectVariableSubscriptionInput, TestObjectVariableSubscription] {
  type Ctor[T] = T

  @js.native
  trait ObjectVariable extends js.Object {
    val id: String
  }

  def newInput(
    input: _root_.relay.generated.Input
  ): _root_.relay.generated.TestObjectVariableSubscriptionInput =
    _root_.relay.generated.TestObjectVariableSubscriptionInput(
      input
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestObjectVariableSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
