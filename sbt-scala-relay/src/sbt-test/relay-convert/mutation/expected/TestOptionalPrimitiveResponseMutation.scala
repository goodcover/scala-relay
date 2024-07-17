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

@js.native
trait TestOptionalPrimitiveResponseMutation extends js.Object {
  val optionalPrimitiveResponse: String | Null
}

object TestOptionalPrimitiveResponseMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestOptionalPrimitiveResponseMutationInput, TestOptionalPrimitiveResponseMutation] {
  type Ctor[T] = T

  def newInput(
    a: String
  ): TestOptionalPrimitiveResponseMutationInput =
    js.Dynamic.literal(
      "a" -> a.asInstanceOf[js.Any]
    ).asInstanceOf[TestOptionalPrimitiveResponseMutationInput]

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalPrimitiveResponseMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
