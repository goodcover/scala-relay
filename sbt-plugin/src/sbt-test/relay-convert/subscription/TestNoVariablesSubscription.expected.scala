package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestNoVariablesSubscription {
    noVariables {
        clientSubscriptionId
    }
}
*/

trait TestNoVariablesSubscriptionInput extends js.Object

@js.native
trait TestNoVariablesSubscription extends js.Object {
  val noVariables: TestNoVariablesSubscription.NoVariables | Null
}

object TestNoVariablesSubscription extends _root_.relay.gql.SubscriptionTaggedNode[TestNoVariablesSubscriptionInput, TestNoVariablesSubscription] {
  type Ctor[T] = T

  @js.native
  trait NoVariables extends js.Object {
    val clientSubscriptionId: String | Null
  }

  def newInput(): TestNoVariablesSubscriptionInput = js.Dynamic.literal().asInstanceOf[TestNoVariablesSubscriptionInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestNoVariablesSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
