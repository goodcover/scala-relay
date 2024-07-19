package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

/*
input ClientTypeInput {
  required: String! @scalajs(clientType: "Required")
  optional: String @scalajs(clientType: "Optional")
  requiredListRequiredElements: [String!]! @scalajs(clientType: "RequiredListRequiredElements")
  requiredListOptionalElements: [String]! @scalajs(clientType: "RequiredListOptionalElements")
  optionalListRequiredElements: [String!] @scalajs(clientType: "OptionalListRequiredElements")
  optionalListOptionalElements: [String] @scalajs(clientType: "OptionalListOptionalElements")
  nested: ClientTypeNestedInput!
}
*/

trait ClientTypeInput extends js.Object {
  val required: String[Required]
  val optional: js.UndefOr[String[Optional] | Null]
  val requiredListRequiredElements: js.Array[String[RequiredListRequiredElements]]
  val requiredListOptionalElements: js.Array[js.UndefOr[String[RequiredListOptionalElements] | Null]]
  val optionalListRequiredElements: js.UndefOr[js.Array[String[OptionalListRequiredElements]] | Null]
  val optionalListOptionalElements: js.UndefOr[js.Array[js.UndefOr[String[OptionalListOptionalElements] | Null]] | Null]
  val nested: ClientTypeNestedInput
}

object ClientTypeInput {
  def apply(
    required: String[Required],
    optional: js.UndefOr[String[Optional] | Null] = js.undefined,
    requiredListRequiredElements: js.Array[String[RequiredListRequiredElements]],
    requiredListOptionalElements: js.Array[js.UndefOr[String[RequiredListOptionalElements] | Null]],
    optionalListRequiredElements: js.UndefOr[js.Array[String[OptionalListRequiredElements]] | Null] = js.undefined,
    optionalListOptionalElements: js.UndefOr[js.Array[js.UndefOr[String[OptionalListOptionalElements] | Null]] | Null] = js.undefined,
    nested: ClientTypeNestedInput
  ): ClientTypeInput =
    js.Dynamic.literal(
      required = required,
      optional = optional,
      requiredListRequiredElements = requiredListRequiredElements,
      requiredListOptionalElements = requiredListOptionalElements,
      optionalListRequiredElements = optionalListRequiredElements,
      optionalListOptionalElements = optionalListOptionalElements,
      nested = nested
    ).asInstanceOf[ClientTypeInput]
}
