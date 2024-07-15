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
  val optional: String[Optional] | Null
  val requiredListRequiredElements: js.Array[String[RequiredListRequiredElements]]
  val requiredListOptionalElements: js.Array[String[RequiredListOptionalElements] | Null]
  val optionalListRequiredElements: js.Array[String[OptionalListRequiredElements]] | Null
  val optionalListOptionalElements: js.Array[String[OptionalListOptionalElements] | Null] | Null
  val nested: ClientTypeNestedInput
}

object ClientTypeInput {
  def apply(
    required: String[Required],
    optional: String[Optional] | Null = null,
    requiredListRequiredElements: js.Array[String[RequiredListRequiredElements]],
    requiredListOptionalElements: js.Array[String[RequiredListOptionalElements] | Null],
    optionalListRequiredElements: js.Array[String[OptionalListRequiredElements]] | Null = null,
    optionalListOptionalElements: js.Array[String[OptionalListOptionalElements] | Null] | Null = null,
    nested: ClientTypeNestedInput
  ): ClientTypeInput =
    js.Dynamic.literal(
      "required" -> required.asInstanceOf[js.Any],
      "optional" -> optional.asInstanceOf[js.Any],
      "requiredListRequiredElements" -> requiredListRequiredElements.asInstanceOf[js.Any],
      "requiredListOptionalElements" -> requiredListOptionalElements.asInstanceOf[js.Any],
      "optionalListRequiredElements" -> optionalListRequiredElements.asInstanceOf[js.Any],
      "optionalListOptionalElements" -> optionalListOptionalElements.asInstanceOf[js.Any],
      "nested" -> nested.asInstanceOf[js.Any]
    ).asInstanceOf[ClientTypeInput]
}
