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
  val id: js.UndefOr[String | Null]
  val number: js.UndefOr[Double | Null]
  val required: Bar
  val optional: js.UndefOr[Bar | Null]
  val requiredListRequiredElements: js.Array[Bar]
  val requiredListOptionalElements: js.Array[js.UndefOr[Bar | Null]]
  val optionalListRequiredElements: js.UndefOr[js.Array[Bar] | Null]
  val optionalListOptionalElements: js.UndefOr[js.Array[js.UndefOr[Bar | Null]] | Null]
  val nested: ClientTypeNestedInput
}

object ClientTypeInput {
  def apply(
    id: js.UndefOr[String | Null] = js.undefined,
    number: js.UndefOr[Double | Null] = js.undefined,
    required: Bar,
    optional: js.UndefOr[Bar | Null] = js.undefined,
    requiredListRequiredElements: js.Array[Bar],
    requiredListOptionalElements: js.Array[js.UndefOr[Bar | Null]],
    optionalListRequiredElements: js.UndefOr[js.Array[Bar] | Null] = js.undefined,
    optionalListOptionalElements: js.UndefOr[js.Array[js.UndefOr[Bar | Null]] | Null] = js.undefined,
    nested: ClientTypeNestedInput
  ): ClientTypeInput =
    js.Dynamic.literal(
      id = id,
      number = number,
      required = required,
      optional = optional,
      requiredListRequiredElements = requiredListRequiredElements,
      requiredListOptionalElements = requiredListOptionalElements,
      optionalListRequiredElements = optionalListRequiredElements,
      optionalListOptionalElements = optionalListOptionalElements,
      nested = nested
    ).asInstanceOf[ClientTypeInput]
}
