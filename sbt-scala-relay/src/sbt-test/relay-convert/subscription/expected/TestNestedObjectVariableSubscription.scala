package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestNestedObjectVariableSubscription($nested: Nested) {
    nestedObjectVariable(nested: $nested) {
        id
    }
}
 */

trait TestNestedObjectVariableSubscriptionInput extends js.Object {
  val nested: Nested | Null
}

object TestNestedObjectVariableSubscriptionInput {
  def apply(
    nested: Nested | Null = null
  ): TestNestedObjectVariableSubscriptionInput =
    js.Dynamic
      .literal(
        nested = nested
      )
      .asInstanceOf[TestNestedObjectVariableSubscriptionInput]
}

@js.native
trait TestNestedObjectVariableSubscription extends js.Object {
  val nestedObjectVariable: TestNestedObjectVariableSubscription.NestedObjectVariable
}

object TestNestedObjectVariableSubscription
    extends _root_.com.goodcover.relay.SubscriptionTaggedNode[
      TestNestedObjectVariableSubscriptionInput,
      TestNestedObjectVariableSubscription
    ] {
  type Ctor[T] = T

  @js.native
  trait NestedObjectVariable extends js.Object {
    val id: String
  }

  def newInput(
    nested: _root_.relay.generated.Nested | Null = null
  ): _root_.relay.generated.TestNestedObjectVariableSubscriptionInput =
    _root_.relay.generated.TestNestedObjectVariableSubscriptionInput(
      nested
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestNestedObjectVariableSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
