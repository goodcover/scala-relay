package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestOptionalObjectVariableQuery($input: Input) {
    objectVariable(input: $input) {
        id
    }
}
*/

trait TestOptionalObjectVariableQueryInput extends js.Object {
  val input: Input | Null
}

object TestOptionalObjectVariableQueryInput {
  def apply(
    input: Input | Null = null
  ): TestOptionalObjectVariableQueryInput =
    js.Dynamic.literal(
      input = input
    ).asInstanceOf[TestOptionalObjectVariableQueryInput]
}

@js.native
trait TestOptionalObjectVariableQuery extends js.Object {
  val objectVariable: TestOptionalObjectVariableQuery.ObjectVariable
}

object TestOptionalObjectVariableQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestOptionalObjectVariableQueryInput, TestOptionalObjectVariableQuery] {
  type Ctor[T] = T

  @js.native
  trait ObjectVariable extends js.Object {
    val id: String
  }

  def newInput(
    input: _root_.relay.generated.Input | Null = null
  ): _root_.relay.generated.TestOptionalObjectVariableQueryInput =
    _root_.relay.generated.TestOptionalObjectVariableQueryInput(
      input
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalObjectVariableQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
