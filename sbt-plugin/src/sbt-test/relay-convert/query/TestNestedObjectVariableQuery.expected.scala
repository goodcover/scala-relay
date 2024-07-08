package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestNestedObjectVariableQuery($query: CheckinSearchInput) {
    checkinSearchQuery(query: $query) {
        query
    }
}
*/

trait TestNestedObjectVariableQueryInput extends js.Object {
  val query: String | Null
  val inputs: js.Array[TestNestedObjectVariableQueryCheckinSearchInput] | Null
}

trait TestNestedObjectVariableQueryCheckinSearchInput extends js.Object {
  val query: String | Null
  val inputs: js.Array[TestNestedObjectVariableQueryCheckinSearchInput] | Null
}

object TestNestedObjectVariableQueryCheckinSearchInput {
  def apply(
    query: String | Null = null,
    inputs: js.Array[TestNestedObjectVariableQueryCheckinSearchInput] | Null = null
  ): TestNestedObjectVariableQueryCheckinSearchInput =
    js.Dynamic.literal(
      "query" -> query.asInstanceOf[js.Any],
      "inputs" -> inputs.asInstanceOf[js.Any]
    ).asInstanceOf[TestNestedObjectVariableQueryCheckinSearchInput]
}

trait TestNestedObjectVariableQueryCheckinSearchInput extends js.Object {
  val query: String | Null
  val inputs: js.Array[TestNestedObjectVariableQueryCheckinSearchInput] | Null
}

object TestNestedObjectVariableQueryCheckinSearchInput {
  def apply(
    query: String | Null = null,
    inputs: js.Array[TestNestedObjectVariableQueryCheckinSearchInput] | Null = null
  ): TestNestedObjectVariableQueryCheckinSearchInput =
    js.Dynamic.literal(
      "query" -> query.asInstanceOf[js.Any],
      "inputs" -> inputs.asInstanceOf[js.Any]
    ).asInstanceOf[TestNestedObjectVariableQueryCheckinSearchInput]
}

@js.native
trait TestNestedObjectVariableQuery extends js.Object {
  val checkinSearchQuery: TestNestedObjectVariableQuery.CheckinSearchQuery | Null
}

object TestNestedObjectVariableQuery extends _root_.relay.gql.QueryTaggedNode[TestNestedObjectVariableQueryInput, TestNestedObjectVariableQuery] {
  type Ctor[T] = T

  @js.native
  trait CheckinSearchQuery extends js.Object {
    val query: String | Null
  }

  def newInput(
    query: String | Null = null,
    inputs: js.Array[TestNestedObjectVariableQueryCheckinSearchInput] | Null = null
  ): TestNestedObjectVariableQueryInput =
    js.Dynamic.literal(
      "query" -> query.asInstanceOf[js.Any],
      "inputs" -> inputs.asInstanceOf[js.Any]
    ).asInstanceOf[TestNestedObjectVariableQueryInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestNestedObjectVariableQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
