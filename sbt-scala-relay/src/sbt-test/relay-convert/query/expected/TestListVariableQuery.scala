package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestListVariableQuery($as: [String!]!) {
    listVariable(as: $as) {
        id
    }
}
*/

trait TestListVariableQueryInput extends js.Object {
  val as: js.Array[String]
}

@js.native
trait TestListVariableQuery extends js.Object {
  val listVariable: TestListVariableQuery.ListVariable
}

object TestListVariableQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestListVariableQueryInput, TestListVariableQuery] {
  type Ctor[T] = T

  @js.native
  trait ListVariable extends js.Object {
    val id: String
  }

  def newInput(
    as: js.Array[String]
  ): TestListVariableQueryInput =
    js.Dynamic.literal(
      "as" -> as.asInstanceOf[js.Any]
    ).asInstanceOf[TestListVariableQueryInput]

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestListVariableQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
