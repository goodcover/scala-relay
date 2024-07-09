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
  val required: String[Required]
  val optional: String[Optional] | Null
  val requiredListRequiredElements: js.Array[String[RequiredListRequiredElements]]
  val requiredListOptionalElements: js.Array[String[RequiredListOptionalElements] | Null]
  val optionalListRequiredElements: js.Array[String[OptionalListRequiredElements]] | Null
  val optionalListOptionalElements: js.Array[String[OptionalListOptionalElements] | Null] | Null
  val nested: TestQueryClientTypeNestedInput
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

object TestQuery extends _root_.relay.gql.QueryTaggedNode[TestQueryInput, TestQuery] {
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
    required: String[Required],
    optional: String[Optional] | Null = null,
    requiredListRequiredElements: js.Array[String[RequiredListRequiredElements]],
    requiredListOptionalElements: js.Array[String[RequiredListOptionalElements] | Null],
    optionalListRequiredElements: js.Array[String[OptionalListRequiredElements]] | Null = null,
    optionalListOptionalElements: js.Array[String[OptionalListOptionalElements] | Null] | Null = null,
    nested: TestQueryClientTypeNestedInput
  ): TestQueryInput =
    js.Dynamic.literal(
      "required" -> required.asInstanceOf[js.Any],
      "optional" -> optional.asInstanceOf[js.Any],
      "requiredListRequiredElements" -> requiredListRequiredElements.asInstanceOf[js.Any],
      "requiredListOptionalElements" -> requiredListOptionalElements.asInstanceOf[js.Any],
      "optionalListRequiredElements" -> optionalListRequiredElements.asInstanceOf[js.Any],
      "optionalListOptionalElements" -> optionalListOptionalElements.asInstanceOf[js.Any],
      "nested" -> nested.asInstanceOf[js.Any]
    ).asInstanceOf[TestQueryInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
