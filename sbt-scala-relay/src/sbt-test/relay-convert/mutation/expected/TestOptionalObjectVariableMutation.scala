package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestOptionalObjectVariableMutation($input: Input) {
    objectVariable(input: $input) {
        id
    }
}
*/

trait TestOptionalObjectVariableMutationInput extends js.Object {
  val input: Input | Null
}

object TestOptionalObjectVariableMutationInput {
  def apply(
    input: Input | Null = null
  ): TestOptionalObjectVariableMutationInput =
    js.Dynamic.literal(
      input = input
    ).asInstanceOf[TestOptionalObjectVariableMutationInput]
}

@js.native
trait TestOptionalObjectVariableMutation extends js.Object {
  val objectVariable: TestOptionalObjectVariableMutation.ObjectVariable
}

object TestOptionalObjectVariableMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestOptionalObjectVariableMutationInput, TestOptionalObjectVariableMutation] {
  type Ctor[T] = T

  @js.native
  trait ObjectVariable extends js.Object {
    val id: String
  }

  def newInput(
    input: _root_.relay.generated.Input | Null = null
  ): _root_.relay.generated.TestOptionalObjectVariableMutationInput =
    _root_.relay.generated.TestOptionalObjectVariableMutationInput(
      input
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalObjectVariableMutation.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
