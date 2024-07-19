package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestOptionalObjectVariableSubscription($input: Input) {
    objectVariable(input: $input) {
        id
    }
}
*/

trait TestOptionalObjectVariableSubscriptionInput extends js.Object {
  val input: js.UndefOr[Input | Null]
}

object TestOptionalObjectVariableSubscriptionInput {
  def apply(
    input: js.UndefOr[Input | Null] = js.undefined
  ): TestOptionalObjectVariableSubscriptionInput =
    js.Dynamic.literal(
      input = input
    ).asInstanceOf[TestOptionalObjectVariableSubscriptionInput]
}

@js.native
trait TestOptionalObjectVariableSubscription extends js.Object {
  val objectVariable: TestOptionalObjectVariableSubscription.ObjectVariable
}

object TestOptionalObjectVariableSubscription extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestOptionalObjectVariableSubscriptionInput, TestOptionalObjectVariableSubscription] {
  type Ctor[T] = T

  @js.native
  trait ObjectVariable extends js.Object {
    val id: String
  }

  def newInput(
    input: js.UndefOr[_root_.relay.generated.Input | Null] = js.undefined
  ): _root_.relay.generated.TestOptionalObjectVariableSubscriptionInput =
    _root_.relay.generated.TestOptionalObjectVariableSubscriptionInput(
      input
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalObjectVariableSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
