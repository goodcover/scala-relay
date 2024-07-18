package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestMultipleVariablesQuery($a: String!, $b: String!) {
    multipleVariables(a: $a, b: $b) {
        id
    }
}
*/

trait TestMultipleVariablesQueryInput extends js.Object {
  val a: String
  val b: String
}

object TestMultipleVariablesQueryInput {
  def apply(
    a: String,
    b: String
  ): TestMultipleVariablesQueryInput =
    js.Dynamic.literal(
      a = a,
      b = b
    ).asInstanceOf[TestMultipleVariablesQueryInput]
}

@js.native
trait TestMultipleVariablesQuery extends js.Object {
  val multipleVariables: TestMultipleVariablesQuery.MultipleVariables
}

object TestMultipleVariablesQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestMultipleVariablesQueryInput, TestMultipleVariablesQuery] {
  type Ctor[T] = T

  @js.native
  trait MultipleVariables extends js.Object {
    val id: String
  }

  def newInput(
    a: String,
    b: String
  ): _root_.relay.generated.TestMultipleVariablesQueryInput =
    _root_.relay.generated.TestMultipleVariablesQueryInput(
      a,
      b
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestMultipleVariablesQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
