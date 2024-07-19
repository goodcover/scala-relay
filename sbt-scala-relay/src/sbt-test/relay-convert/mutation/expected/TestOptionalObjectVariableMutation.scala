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
  val input: js.UndefOr[Input | Null]
}

object TestOptionalObjectVariableMutationInput {
  def apply(
    input: js.UndefOr[Input | Null] = js.undefined
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
    input: js.UndefOr[_root_.relay.generated.Input | Null] = js.undefined
  ): _root_.relay.generated.TestOptionalObjectVariableMutationInput =
    _root_.relay.generated.TestOptionalObjectVariableMutationInput(
      input
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalObjectVariableMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
