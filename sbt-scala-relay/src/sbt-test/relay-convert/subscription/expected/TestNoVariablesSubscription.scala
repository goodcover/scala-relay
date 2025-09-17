package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestNoVariablesSubscription {
    noVariables {
        id
    }
}
 */

trait TestNoVariablesSubscriptionInput extends js.Object

object TestNoVariablesSubscriptionInput {
  def apply(): TestNoVariablesSubscriptionInput = js.Dynamic.literal().asInstanceOf[TestNoVariablesSubscriptionInput]
}

@js.native
trait TestNoVariablesSubscription extends js.Object {
  val noVariables: TestNoVariablesSubscription.NoVariables
}

object TestNoVariablesSubscription
    extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestNoVariablesSubscriptionInput, TestNoVariablesSubscription] {
  type Ctor[T] = T

  @js.native
  trait NoVariables extends js.Object {
    val id: String
  }

  def newInput(): _root_.relay.generated.TestNoVariablesSubscriptionInput =
    _root_.relay.generated.TestNoVariablesSubscriptionInput()

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestNoVariablesSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
