package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestPrimitiveVariableQuery($a: String!) {
    primitiveVariable(a: $a) {
        id
    }
}
*/

@js.native
trait TestPrimitiveVariableQuery extends js.Object {
  val primitiveVariable: TestPrimitiveVariableQuery.PrimitiveVariable | Null
}

object TestPrimitiveVariableQuery extends _root_.relay.gql.QueryTaggedNode[Foo, TestPrimitiveVariableQuery] {
  type Ctor[T] = T

  @js.native
  trait PrimitiveVariable extends js.Object {
    val id: String
  }

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated/TestPrimitiveVariableQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
