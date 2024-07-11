package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestObjectVariableQuery($filter: ItemFilterInput!) {
    items(filter: $filter) {
        date
    }
}
*/

trait TestObjectVariableQueryInput extends js.Object {
  val filter: TestObjectVariableQueryItemFilterInput
}

trait TestObjectVariableQueryItemFilterInput extends js.Object {
  val date: String | Null
}

object TestObjectVariableQueryItemFilterInput {
  def apply(
    date: String | Null = null
  ): TestObjectVariableQueryItemFilterInput =
    js.Dynamic.literal(
      "date" -> date.asInstanceOf[js.Any]
    ).asInstanceOf[TestObjectVariableQueryItemFilterInput]
}

@js.native
trait TestObjectVariableQuery extends js.Object {
  val items: TestObjectVariableQuery.Items | Null
}

object TestObjectVariableQuery extends _root_.relay.gql.QueryTaggedNode[TestObjectVariableQueryInput, TestObjectVariableQuery] {
  type Ctor[T] = T

  @js.native
  trait Items extends js.Object {
    val date: String | Null
  }

  def newInput(
    filter: TestObjectVariableQueryItemFilterInput
  ): TestObjectVariableQueryInput =
    js.Dynamic.literal(
      "filter" -> filter.asInstanceOf[js.Any]
    ).asInstanceOf[TestObjectVariableQueryInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("./__generated__/TestObjectVariableQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
