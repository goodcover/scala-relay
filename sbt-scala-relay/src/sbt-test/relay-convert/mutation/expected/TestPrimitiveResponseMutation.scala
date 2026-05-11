package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestPrimitiveResponseMutation($a: String!) {
    primitiveResponse(a: $a)
}
*/

trait TestPrimitiveResponseMutationInput extends js.Object {
  val a: String
}

object TestPrimitiveResponseMutationInput {
  def apply(
    a: String
  ): TestPrimitiveResponseMutationInput =
    js.Dynamic.literal(
      a = a
    ).asInstanceOf[TestPrimitiveResponseMutationInput]
}

@js.native
trait TestPrimitiveResponseMutation extends js.Object {
  val primitiveResponse: String
}

object TestPrimitiveResponseMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestPrimitiveResponseMutationInput, TestPrimitiveResponseMutation] {
  type Ctor[T] = T

  def newInput(
    a: String
  ): _root_.relay.generated.TestPrimitiveResponseMutationInput =
    _root_.relay.generated.TestPrimitiveResponseMutationInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestPrimitiveResponseMutation.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
