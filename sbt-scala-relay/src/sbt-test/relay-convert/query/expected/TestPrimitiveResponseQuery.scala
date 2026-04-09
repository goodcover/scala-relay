package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestPrimitiveResponseQuery($a: String!) {
    primitiveResponse(a: $a)
}
 */

trait TestPrimitiveResponseQueryInput extends js.Object {
  val a: String
}

object TestPrimitiveResponseQueryInput {
  def apply(
    a: String
  ): TestPrimitiveResponseQueryInput =
    js.Dynamic
      .literal(
        a = a
      )
      .asInstanceOf[TestPrimitiveResponseQueryInput]
}

@js.native
trait TestPrimitiveResponseQuery extends js.Object {
  val primitiveResponse: String
}

object TestPrimitiveResponseQuery
    extends _root_.com.goodcover.relay.QueryTaggedNode[TestPrimitiveResponseQueryInput, TestPrimitiveResponseQuery] {
  type Ctor[T] = T

  def newInput(
    a: String
  ): _root_.relay.generated.TestPrimitiveResponseQueryInput =
    _root_.relay.generated.TestPrimitiveResponseQueryInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestPrimitiveResponseQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
