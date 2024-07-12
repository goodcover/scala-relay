package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestQuery($input: ClientTypeInput!) {
    clientType(input: $input) {
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
  val required: String[Required]
  val optional: String[Optional] | Null
  val requiredListRequiredElements: js.Array[String[RequiredListRequiredElements]]
  val requiredListOptionalElements: js.Array[String[RequiredListOptionalElements] | Null]
  val optionalListRequiredElements: js.Array[String[OptionalListRequiredElements]] | Null
  val optionalListOptionalElements: js.Array[String[OptionalListOptionalElements] | Null] | Null
  val nested: TestQueryClientTypeNestedInput
}

object TestQueryClientTypeInput {
  def apply(
    required: String[Required],
    optional: String[Optional] | Null = null,
    requiredListRequiredElements: js.Array[String[RequiredListRequiredElements]],
    requiredListOptionalElements: js.Array[String[RequiredListOptionalElements] | Null],
    optionalListRequiredElements: js.Array[String[OptionalListRequiredElements]] | Null = null,
    optionalListOptionalElements: js.Array[String[OptionalListOptionalElements] | Null] | Null = null,
    nested: TestQueryClientTypeNestedInput
  ): TestQueryClientTypeInput =
    js.Dynamic.literal(
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
  val nested: String[Nested]
}

object TestQueryClientTypeNestedInput {
  def apply(
    nested: String[Nested]
  ): TestQueryClientTypeNestedInput =
    js.Dynamic.literal(
      "nested" -> nested.asInstanceOf[js.Any]
    ).asInstanceOf[TestQueryClientTypeNestedInput]
}

@js.native
trait TestQuery extends js.Object {
  val clientType: TestQuery.ClientType | Null
}

object TestQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestQueryInput, TestQuery] {
  type Ctor[T] = T

  @js.native
  trait ClientTypeObject extends js.Object {
    val required: String[Object]
  }

  @js.native
  trait ClientTypeInterface extends js.Object {
    val required: String[Interface]
  }

  @js.native
  trait ClientType extends js.Object {
    val required: String[Required]
    val optional: String[Optional] | Null
    val requiredListRequiredElements: js.Array[String[RequiredListRequiredElements]]
    val requiredListOptionalElements: js.Array[String[RequiredListOptionalElements] | Null]
    val optionalListRequiredElements: js.Array[String[OptionalListRequiredElements]] | Null
    val optionalListOptionalElements: js.Array[String[OptionalListOptionalElements] | Null] | Null
    val `object`: ClientTypeObject
    val interface: ClientTypeInterface
  }

  def newInput(
    input: TestQueryClientTypeInput
  ): TestQueryInput =
    js.Dynamic.literal(
      "input" -> input.asInstanceOf[js.Any]
    ).asInstanceOf[TestQueryInput]

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
