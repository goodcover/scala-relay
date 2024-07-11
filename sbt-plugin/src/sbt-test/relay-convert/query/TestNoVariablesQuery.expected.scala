package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestNoVariablesQuery {
    noVariables {
        id
    }
}
*/

trait TestNoVariablesQueryInput extends js.Object

@js.native
trait TestNoVariablesQuery extends js.Object {
  val noVariables: TestNoVariablesQuery.NoVariables | Null
}

object TestNoVariablesQuery extends _root_.relay.gql.QueryTaggedNode[TestNoVariablesQueryInput, TestNoVariablesQuery] {
  type Ctor[T] = T

  @js.native
  trait NoVariables extends js.Object {
    val id: String
  }

  def newInput(): TestNoVariablesQueryInput = js.Dynamic.literal().asInstanceOf[TestNoVariablesQueryInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("./__generated__/TestNoVariablesQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
