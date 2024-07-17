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
  val input: TestOptionalObjectVariableMutationInput | Null
}

trait TestOptionalObjectVariableMutationInput extends js.Object {
  val a: String
}

object TestOptionalObjectVariableMutationInput {
  def apply(
    a: String
  ): TestOptionalObjectVariableMutationInput =
    js.Dynamic.literal(
      "a" -> a.asInstanceOf[js.Any]
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
    input: TestOptionalObjectVariableMutationInput | Null = null
  ): TestOptionalObjectVariableMutationInput =
    js.Dynamic.literal(
      "input" -> input.asInstanceOf[js.Any]
    ).asInstanceOf[TestOptionalObjectVariableMutationInput]

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalObjectVariableMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
