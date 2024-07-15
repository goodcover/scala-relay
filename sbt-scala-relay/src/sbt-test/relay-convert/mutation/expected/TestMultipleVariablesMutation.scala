package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestMultipleVariablesMutation($a: String!, $b: String!) {
    multipleVariables(a: $a, b: $b) {
        id
    }
}
*/

trait TestMultipleVariablesMutationInput extends js.Object {
  val a: String
  val b: String
}

object TestMultipleVariablesMutationInput {
  def apply(
    a: String,
    b: String
  ): TestMultipleVariablesMutationInput =
    js.Dynamic.literal(
      "a" -> a.asInstanceOf[js.Any],
      "b" -> b.asInstanceOf[js.Any]
    ).asInstanceOf[TestMultipleVariablesMutationInput]
}

@js.native
trait TestMultipleVariablesMutation extends js.Object {
  val multipleVariables: TestMultipleVariablesMutation.MultipleVariables
}

object TestMultipleVariablesMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestMultipleVariablesMutationInput, TestMultipleVariablesMutation] {
  type Ctor[T] = T

  @js.native
  trait MultipleVariables extends js.Object {
    val id: String
  }

  def newInput(
    a: String,
    b: String
  ): _root_.relay.generated.TestMultipleVariablesMutationInput =
    _root_.relay.generated.TestMultipleVariablesMutationInput(
      a,
      b
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestMultipleVariablesMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
