package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
subscription TestOptionalPrimitiveVariableSubscription($a: String) {
    primitiveVariable(a: $a) {
        id
    }
}
 */

trait TestOptionalPrimitiveVariableSubscriptionInput extends js.Object {
  val a: String | Null
}

object TestOptionalPrimitiveVariableSubscriptionInput {
  def apply(
    a: String | Null = null
  ): TestOptionalPrimitiveVariableSubscriptionInput =
    js.Dynamic
      .literal(
        a = a
      )
      .asInstanceOf[TestOptionalPrimitiveVariableSubscriptionInput]
}

@js.native
trait TestOptionalPrimitiveVariableSubscription extends js.Object {
  val primitiveVariable: TestOptionalPrimitiveVariableSubscription.PrimitiveVariable
}

object TestOptionalPrimitiveVariableSubscription
    extends _root_.com.goodcover.relay.SubscriptionTaggedNode[
      TestOptionalPrimitiveVariableSubscriptionInput,
      TestOptionalPrimitiveVariableSubscription
    ] {
  type Ctor[T] = T

  @js.native
  trait PrimitiveVariable extends js.Object {
    val id: String
  }

  def newInput(
    a: String | Null = null
  ): _root_.relay.generated.TestOptionalPrimitiveVariableSubscriptionInput =
    _root_.relay.generated.TestOptionalPrimitiveVariableSubscriptionInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalPrimitiveVariableSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
