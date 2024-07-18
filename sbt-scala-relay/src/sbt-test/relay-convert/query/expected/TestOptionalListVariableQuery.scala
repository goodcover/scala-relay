package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestOptionalListVariableQuery($as: [String!]) {
    listVariable(as: $as) {
        id
    }
}
*/

trait TestOptionalListVariableQueryInput extends js.Object {
  val as: js.Array[String] | Null
}

object TestOptionalListVariableQueryInput {
  def apply(
    as: js.Array[String] | Null = null
  ): TestOptionalListVariableQueryInput =
    js.Dynamic.literal(
      as = as
    ).asInstanceOf[TestOptionalListVariableQueryInput]
}

@js.native
trait TestOptionalListVariableQuery extends js.Object {
  val listVariable: TestOptionalListVariableQuery.ListVariable
}

object TestOptionalListVariableQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestOptionalListVariableQueryInput, TestOptionalListVariableQuery] {
  type Ctor[T] = T

  @js.native
  trait ListVariable extends js.Object {
    val id: String
  }

  def newInput(
    as: js.Array[String] | Null = null
  ): _root_.relay.generated.TestOptionalListVariableQueryInput =
    _root_.relay.generated.TestOptionalListVariableQueryInput(
      as
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalListVariableQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
