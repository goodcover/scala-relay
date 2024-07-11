package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestPrimitiveVariableMutation($a: String!) {
    primitiveVariable(a: $a) {
        clientMutationId
    }
}
*/

@js.native
trait TestPrimitiveVariableMutation extends js.Object {
  val primitiveVariable: TestPrimitiveVariableMutation.PrimitiveVariable | Null
}

object TestPrimitiveVariableMutation extends _root_.relay.gql.MutationTaggedNode[String, TestPrimitiveVariableMutation] {
  type Ctor[T] = T

  @js.native
  trait PrimitiveVariable extends js.Object {
    val clientMutationId: String | Null
  }

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated/TestPrimitiveVariableMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
