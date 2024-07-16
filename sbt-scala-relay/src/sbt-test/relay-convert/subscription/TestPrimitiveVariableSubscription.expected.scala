package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestPrimitiveVariableSubscription($a: String!) {
    primitiveVariable(a: $a) {
        clientSubscriptionId
    }
}
*/

trait TestPrimitiveVariableSubscriptionInput extends js.Object {
  val a: String
}

@js.native
trait TestPrimitiveVariableSubscription extends js.Object {
  val primitiveVariable: TestPrimitiveVariableSubscription.PrimitiveVariable | Null
}

object TestPrimitiveVariableSubscription extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestPrimitiveVariableSubscriptionInput, TestPrimitiveVariableSubscription] {
  type Ctor[T] = T

  @js.native
  trait PrimitiveVariable extends js.Object {
    val clientSubscriptionId: String | Null
  }

  def newInput(
    a: String
  ): TestPrimitiveVariableSubscriptionInput =
    js.Dynamic.literal(
      "a" -> a.asInstanceOf[js.Any]
    ).asInstanceOf[TestPrimitiveVariableSubscriptionInput]

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestPrimitiveVariableSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
