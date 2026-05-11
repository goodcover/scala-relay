package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestPrimitiveVariableSubscription($a: String!) {
    primitiveVariable(a: $a) {
        id
    }
}
*/

trait TestPrimitiveVariableSubscriptionInput extends js.Object {
  val a: String
}

object TestPrimitiveVariableSubscriptionInput {
  def apply(
    a: String
  ): TestPrimitiveVariableSubscriptionInput =
    js.Dynamic.literal(
      a = a
    ).asInstanceOf[TestPrimitiveVariableSubscriptionInput]
}

@js.native
trait TestPrimitiveVariableSubscription extends js.Object {
  val primitiveVariable: TestPrimitiveVariableSubscription.PrimitiveVariable
}

object TestPrimitiveVariableSubscription extends _root_.com.goodcover.relay.SubscriptionTaggedNode[TestPrimitiveVariableSubscriptionInput, TestPrimitiveVariableSubscription] {
  type Ctor[T] = T

  @js.native
  trait PrimitiveVariable extends js.Object {
    val id: String
  }

  def newInput(
    a: String
  ): _root_.relay.generated.TestPrimitiveVariableSubscriptionInput =
    _root_.relay.generated.TestPrimitiveVariableSubscriptionInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestPrimitiveVariableSubscription.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
