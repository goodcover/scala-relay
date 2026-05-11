package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestOptionalPrimitiveResponseQuery($a: String!) {
    optionalPrimitiveResponse(a: $a)
}
*/

trait TestOptionalPrimitiveResponseQueryInput extends js.Object {
  val a: String
}

object TestOptionalPrimitiveResponseQueryInput {
  def apply(
    a: String
  ): TestOptionalPrimitiveResponseQueryInput =
    js.Dynamic.literal(
      a = a
    ).asInstanceOf[TestOptionalPrimitiveResponseQueryInput]
}

@js.native
trait TestOptionalPrimitiveResponseQuery extends js.Object {
  val optionalPrimitiveResponse: String | Null
}

object TestOptionalPrimitiveResponseQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestOptionalPrimitiveResponseQueryInput, TestOptionalPrimitiveResponseQuery] {
  type Ctor[T] = T

  def newInput(
    a: String
  ): _root_.relay.generated.TestOptionalPrimitiveResponseQueryInput =
    _root_.relay.generated.TestOptionalPrimitiveResponseQueryInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalPrimitiveResponseQuery.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
