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

@js.native
trait TestPrimitiveVariableSubscription extends js.Object {
  val primitiveVariable: TestPrimitiveVariableSubscription.PrimitiveVariable | Null
}

object TestPrimitiveVariableSubscription extends _root_.relay.gql.SubscriptionTaggedNode[String, TestPrimitiveVariableSubscription] {
  type Ctor[T] = T

  @js.native
  trait PrimitiveVariable extends js.Object {
    val clientSubscriptionId: String | Null
  }

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestPrimitiveVariableSubscription.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
