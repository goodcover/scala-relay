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

trait TestPrimitiveVariableQueryInput extends js.Object {
  val a: String
}

object TestPrimitiveVariableQueryInput {
  def apply(
    a: String
  ): TestPrimitiveVariableQueryInput =
    js.Dynamic
      .literal(
        a = a
      )
      .asInstanceOf[TestPrimitiveVariableQueryInput]
}

@js.native
trait TestPrimitiveVariableQuery extends js.Object {
  val primitiveVariable: TestPrimitiveVariableQuery.PrimitiveVariable
}

object TestPrimitiveVariableQuery
    extends _root_.com.goodcover.relay.QueryTaggedNode[TestPrimitiveVariableQueryInput, TestPrimitiveVariableQuery] {
  type Ctor[T] = T

  @js.native
  trait PrimitiveVariable extends js.Object {
    val id: String
  }

  def newInput(
    a: String
  ): _root_.relay.generated.TestPrimitiveVariableQueryInput =
    _root_.relay.generated.TestPrimitiveVariableQueryInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestPrimitiveVariableQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
