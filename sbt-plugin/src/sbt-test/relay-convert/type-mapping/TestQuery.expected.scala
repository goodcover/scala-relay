package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestQuery($input: ClientTypeInput!) {
    clientType(input: $input) {
        id
        number
        required
        optional
        requiredListRequiredElements
        requiredListOptionalElements
        optionalListRequiredElements
        optionalListOptionalElements
    }
}
*/

trait TestQueryInput extends js.Object {
  val id: String | Null
  val number: Double | Null
  val required: Bar
  val optional: Bar | Null
  val requiredListRequiredElements: js.Array[Bar]
  val optionalListRequiredElements: js.Array[Bar] | Null
  val requiredListOptionalElements: js.Array[Bar | Null]
  val optionalListOptionalElements: js.Array[Bar | Null] | Null
}

@js.native
trait TestQuery extends js.Object {
  val clientType: TestQuery.ClientType | Null
}

object TestQuery extends _root_.relay.gql.QueryTaggedNode[TestQueryInput, TestQuery] {
  type Ctor[T] = T

  @js.native
  trait ClientType extends js.Object {
    val id: String | Null
    val number: Double | Null
    val required: Bar
    val optional: Bar | Null
    val requiredListRequiredElements: js.Array[Bar]
    val requiredListOptionalElements: js.Array[Bar | Null]
    val optionalListRequiredElements: js.Array[Bar] | Null
    val optionalListOptionalElements: js.Array[Bar | Null] | Null
  }

  def newInput(
    id: String | Null = null,
    number: Double | Null = null,
    required: Bar,
    optional: Bar | Null = null,
    requiredListRequiredElements: js.Array[Bar],
    optionalListRequiredElements: js.Array[Bar] | Null = null,
    requiredListOptionalElements: js.Array[Bar | Null],
    optionalListOptionalElements: js.Array[Bar | Null] | Null = null
  ): TestQueryInput =
    js.Dynamic.literal(
      "id" -> id.asInstanceOf[js.Any],
      "number" -> number.asInstanceOf[js.Any],
      "required" -> required.asInstanceOf[js.Any],
      "optional" -> optional.asInstanceOf[js.Any],
      "requiredListRequiredElements" -> requiredListRequiredElements.asInstanceOf[js.Any],
      "optionalListRequiredElements" -> optionalListRequiredElements.asInstanceOf[js.Any],
      "requiredListOptionalElements" -> requiredListOptionalElements.asInstanceOf[js.Any],
      "optionalListOptionalElements" -> optionalListOptionalElements.asInstanceOf[js.Any]
    ).asInstanceOf[TestQueryInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
