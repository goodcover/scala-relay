package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestQuery($input: ClientTypeInput!) {
    clientType(input: $input) {
        required
        optional
    }
}
*/

trait TestQueryInput extends js.Object {
  val required: String[Required]
  val optional: String[Optional] | Null
}

@js.native
trait TestQuery extends js.Object {
  val clientType: TestQuery.ClientType | Null
}

object TestQuery extends _root_.relay.gql.QueryTaggedNode[TestQueryInput, TestQuery] {
  type Ctor[T] = T

  @js.native
  trait ClientType extends js.Object {
    val required: String[Required]
    val optional: String[Optional] | Null
  }

  def newInput(
    required: String[Required],
    optional: String[Optional] | Null = null
  ): TestQueryInput =
    js.Dynamic.literal(
      "required" -> required.asInstanceOf[js.Any],
      "optional" -> optional.asInstanceOf[js.Any]
    ).asInstanceOf[TestQueryInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
