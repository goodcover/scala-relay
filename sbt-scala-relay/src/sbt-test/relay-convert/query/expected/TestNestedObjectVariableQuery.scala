package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestNestedObjectVariableQuery($nested: Nested) {
    nestedObjectVariable(nested: $nested) {
        id
    }
}
*/

trait TestNestedObjectVariableQueryInput extends js.Object {
  val nested: Nested | Null
}

object TestNestedObjectVariableQueryInput {
  def apply(
    nested: Nested | Null = null
  ): TestNestedObjectVariableQueryInput =
    js.Dynamic.literal(
      "nested" -> nested.asInstanceOf[js.Any]
    ).asInstanceOf[TestNestedObjectVariableQueryInput]
}

@js.native
trait TestNestedObjectVariableQuery extends js.Object {
  val nestedObjectVariable: TestNestedObjectVariableQuery.NestedObjectVariable
}

object TestNestedObjectVariableQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestNestedObjectVariableQueryInput, TestNestedObjectVariableQuery] {
  type Ctor[T] = T

  @js.native
  trait NestedObjectVariable extends js.Object {
    val id: String
  }

  def newInput(
    nested: _root_.relay.generated.Nested | Null = null
  ): _root_.relay.generated.TestNestedObjectVariableQueryInput =
    _root_.relay.generated.TestNestedObjectVariableQueryInput(
      nested
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestNestedObjectVariableQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
