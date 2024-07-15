package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestNestedObjectVariableMutation($nested: Nested) {
    nestedObjectVariable(nested: $nested) {
        id
    }
}
*/

trait TestNestedObjectVariableMutationInput extends js.Object {
  val nested: Nested | Null
}

object TestNestedObjectVariableMutationInput {
  def apply(
    nested: Nested | Null = null
  ): TestNestedObjectVariableMutationInput =
    js.Dynamic.literal(
      "nested" -> nested.asInstanceOf[js.Any]
    ).asInstanceOf[TestNestedObjectVariableMutationInput]
}

@js.native
trait TestNestedObjectVariableMutation extends js.Object {
  val nestedObjectVariable: TestNestedObjectVariableMutation.NestedObjectVariable
}

object TestNestedObjectVariableMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestNestedObjectVariableMutationInput, TestNestedObjectVariableMutation] {
  type Ctor[T] = T

  @js.native
  trait NestedObjectVariable extends js.Object {
    val id: String
  }

  def newInput(
    nested: _root_.relay.generated.Nested | Null = null
  ): _root_.relay.generated.TestNestedObjectVariableMutationInput =
    _root_.relay.generated.TestNestedObjectVariableMutationInput(
      nested
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestNestedObjectVariableMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
