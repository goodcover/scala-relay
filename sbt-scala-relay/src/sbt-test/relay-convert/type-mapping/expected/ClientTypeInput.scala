package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

/*
input ClientTypeInput {
  id: ID
  number: Float
  required: Foo!
  optional: Foo
  requiredListRequiredElements: [Foo!]!
  requiredListOptionalElements: [Foo]!
  optionalListRequiredElements: [Foo!]
  optionalListOptionalElements: [Foo]
  nested: ClientTypeNestedInput!
}
*/

trait ClientTypeInput extends js.Object {
  val id: String | Null
  val number: Double | Null
  val required: Bar
  val optional: Bar | Null
  val requiredListRequiredElements: js.Array[Bar]
  val requiredListOptionalElements: js.Array[Bar | Null]
  val optionalListRequiredElements: js.Array[Bar] | Null
  val optionalListOptionalElements: js.Array[Bar | Null] | Null
  val nested: ClientTypeNestedInput
}

object ClientTypeInput {
  def apply(
    id: String | Null = null,
    number: Double | Null = null,
    required: Bar,
    optional: Bar | Null = null,
    requiredListRequiredElements: js.Array[Bar],
    requiredListOptionalElements: js.Array[Bar | Null],
    optionalListRequiredElements: js.Array[Bar] | Null = null,
    optionalListOptionalElements: js.Array[Bar | Null] | Null = null,
    nested: ClientTypeNestedInput
  ): ClientTypeInput =
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
    ).asInstanceOf[ClientTypeInput]
}
