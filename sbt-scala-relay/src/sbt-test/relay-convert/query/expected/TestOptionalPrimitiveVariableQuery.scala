package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestOptionalPrimitiveVariableQuery($a: String) {
    primitiveVariable(a: $a) {
        id
    }
}
*/

trait TestOptionalPrimitiveVariableQueryInput extends js.Object {
  val a: String | Null
}

@js.native
trait TestOptionalPrimitiveVariableQuery extends js.Object {
  val primitiveVariable: TestOptionalPrimitiveVariableQuery.PrimitiveVariable
}

object TestOptionalPrimitiveVariableQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestOptionalPrimitiveVariableQueryInput, TestOptionalPrimitiveVariableQuery] {
  type Ctor[T] = T

  @js.native
  trait PrimitiveVariable extends js.Object {
    val id: String
  }

  def newInput(
    a: String | Null = null
  ): TestOptionalPrimitiveVariableQueryInput =
    js.Dynamic.literal(
      "a" -> a.asInstanceOf[js.Any]
    ).asInstanceOf[TestOptionalPrimitiveVariableQueryInput]

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalPrimitiveVariableQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
