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
        object {
            required
        }
        interface {
            required
        }
    }
}
*/

trait TestQueryInput extends js.Object {
  val input: TestQueryClientTypeInput
}

trait TestQueryClientTypeInput extends js.Object {
  val id: String | Null
  val number: Double | Null
  val required: Bar
  val optional: Bar | Null
  val requiredListRequiredElements: js.Array[Bar]
  val requiredListOptionalElements: js.Array[Bar | Null]
  val optionalListRequiredElements: js.Array[Bar] | Null
  val optionalListOptionalElements: js.Array[Bar | Null] | Null
  val nested: TestQueryClientTypeNestedInput
}

object TestQueryClientTypeInput {
  def apply(
    id: String | Null = null,
    number: Double | Null = null,
    required: Bar,
    optional: Bar | Null = null,
    requiredListRequiredElements: js.Array[Bar],
    requiredListOptionalElements: js.Array[Bar | Null],
    optionalListRequiredElements: js.Array[Bar] | Null = null,
    optionalListOptionalElements: js.Array[Bar | Null] | Null = null,
    nested: TestQueryClientTypeNestedInput
  ): TestQueryClientTypeInput =
    js.Dynamic.literal(
      "id" -> id.asInstanceOf[js.Any],
      "number" -> number.asInstanceOf[js.Any],
      "required" -> required.asInstanceOf[js.Any],
      "optional" -> optional.asInstanceOf[js.Any],
      "requiredListRequiredElements" -> requiredListRequiredElements.asInstanceOf[js.Any],
      "requiredListOptionalElements" -> requiredListOptionalElements.asInstanceOf[js.Any],
      "optionalListRequiredElements" -> optionalListRequiredElements.asInstanceOf[js.Any],
      "optionalListOptionalElements" -> optionalListOptionalElements.asInstanceOf[js.Any],
      "nested" -> nested.asInstanceOf[js.Any]
    ).asInstanceOf[TestQueryClientTypeInput]
}

trait TestQueryClientTypeNestedInput extends js.Object {
  val nested: Bar
}

object TestQueryClientTypeNestedInput {
  def apply(
    nested: Bar
  ): TestQueryClientTypeNestedInput =
    js.Dynamic.literal(
      "nested" -> nested.asInstanceOf[js.Any]
    ).asInstanceOf[TestQueryClientTypeNestedInput]
}

@js.native
trait TestQuery extends js.Object {
  val clientType: TestQuery.ClientType | Null
}

object TestQuery extends _root_.relay.gql.QueryTaggedNode[TestQueryInput, TestQuery] {
  type Ctor[T] = T

  @js.native
  trait ClientTypeObject extends js.Object {
    val required: Bar
  }

  @js.native
  trait ClientTypeInterface extends js.Object {
    val required: Bar
  }

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
    val `object`: ClientTypeObject
    val interface: ClientTypeInterface
  }

  def newInput(
    input: TestQueryClientTypeInput
  ): TestQueryInput =
    js.Dynamic.literal(
      "input" -> input.asInstanceOf[js.Any]
    ).asInstanceOf[TestQueryInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated/TestQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
