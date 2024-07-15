package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestMultipleVariablesSubscription($a: String!, $b: String!) {
    multipleVariables(a: $a, b: $b) {
        id
    }
}
*/

trait TestMultipleVariablesSubscriptionInput extends js.Object {
  val a: String
  val b: String
}

object TestMultipleVariablesSubscriptionInput {
  def apply(
    a: String,
    b: String
  ): TestMultipleVariablesSubscriptionInput =
    js.Dynamic.literal(
      "a" -> a.asInstanceOf[js.Any],
      "b" -> b.asInstanceOf[js.Any]
    ).asInstanceOf[TestMultipleVariablesSubscriptionInput]
}

@js.native
trait TestMultipleVariablesSubscription extends js.Object {
  val multipleVariables: TestMultipleVariablesSubscription.MultipleVariables
}

object TestMultipleVariablesSubscription extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestMultipleVariablesSubscriptionInput, TestMultipleVariablesSubscription] {
  type Ctor[T] = T

  @js.native
  trait MultipleVariables extends js.Object {
    val id: String
  }

  def newInput(
    a: String,
    b: String
  ): _root_.relay.generated.TestMultipleVariablesSubscriptionInput =
    _root_.relay.generated.TestMultipleVariablesSubscriptionInput(
      a,
      b
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestMultipleVariablesSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
