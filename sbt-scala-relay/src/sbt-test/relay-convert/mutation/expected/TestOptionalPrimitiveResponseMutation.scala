package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestOptionalPrimitiveResponseMutation($a: String!) {
    optionalPrimitiveResponse(a: $a)
}
*/

trait TestOptionalPrimitiveResponseMutationInput extends js.Object {
  val a: String
}

object TestOptionalPrimitiveResponseMutationInput {
  def apply(
    a: String
  ): TestOptionalPrimitiveResponseMutationInput =
    js.Dynamic.literal(
      a = a
    ).asInstanceOf[TestOptionalPrimitiveResponseMutationInput]
}

@js.native
trait TestOptionalPrimitiveResponseMutation extends js.Object {
  val optionalPrimitiveResponse: String | Null
}

object TestOptionalPrimitiveResponseMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestOptionalPrimitiveResponseMutationInput, TestOptionalPrimitiveResponseMutation] {
  type Ctor[T] = T

  def newInput(
    a: String
  ): _root_.relay.generated.TestOptionalPrimitiveResponseMutationInput =
    _root_.relay.generated.TestOptionalPrimitiveResponseMutationInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalPrimitiveResponseMutation.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
