package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestObjectVariableMutation($input: Input!) {
    objectVariable(input: $input) {
        id
    }
}
*/

trait TestObjectVariableMutationInput extends js.Object {
  val input: TestObjectVariableMutationInput
}

trait TestObjectVariableMutationInput extends js.Object {
  val a: String
}

object TestObjectVariableMutationInput {
  def apply(
    a: String
  ): TestObjectVariableMutationInput =
    js.Dynamic.literal(
      "a" -> a.asInstanceOf[js.Any]
    ).asInstanceOf[TestObjectVariableMutationInput]
}

@js.native
trait TestObjectVariableMutation extends js.Object {
  val objectVariable: TestObjectVariableMutation.ObjectVariable
}

object TestObjectVariableMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestObjectVariableMutationInput, TestObjectVariableMutation] {
  type Ctor[T] = T

  @js.native
  trait ObjectVariable extends js.Object {
    val id: String
  }

  def newInput(
    input: TestObjectVariableMutationInput
  ): TestObjectVariableMutationInput =
    js.Dynamic.literal(
      "input" -> input.asInstanceOf[js.Any]
    ).asInstanceOf[TestObjectVariableMutationInput]

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestObjectVariableMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
