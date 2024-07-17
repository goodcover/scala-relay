package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestObjectVariableQuery($input: Input!) {
    objectVariable(input: $input) {
        id
    }
}
*/

trait TestObjectVariableQueryInput extends js.Object {
  val input: Input
}

object TestObjectVariableQueryInput {
  def apply(
    input: Input
  ): TestObjectVariableQueryInput =
    js.Dynamic.literal(
      "input" -> input.asInstanceOf[js.Any]
    ).asInstanceOf[TestObjectVariableQueryInput]
}

@js.native
trait TestObjectVariableQuery extends js.Object {
  val objectVariable: TestObjectVariableQuery.ObjectVariable
}

object TestObjectVariableQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestObjectVariableQueryInput, TestObjectVariableQuery] {
  type Ctor[T] = T

  @js.native
  trait ObjectVariable extends js.Object {
    val id: String
  }

  def newInput(
    input: _root_.relay.generated.Input
  ): _root_.relay.generated.TestObjectVariableQueryInput =
    _root_.relay.generated.TestObjectVariableQueryInput(
      input
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestObjectVariableQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
