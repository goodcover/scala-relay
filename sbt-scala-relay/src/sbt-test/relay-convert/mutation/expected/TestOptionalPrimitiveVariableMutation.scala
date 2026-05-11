package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestOptionalPrimitiveVariableMutation($a: String) {
    primitiveVariable(a: $a) {
        id
    }
}
*/

trait TestOptionalPrimitiveVariableMutationInput extends js.Object {
  val a: String | Null
}

object TestOptionalPrimitiveVariableMutationInput {
  def apply(
    a: String | Null = null
  ): TestOptionalPrimitiveVariableMutationInput =
    js.Dynamic.literal(
      a = a
    ).asInstanceOf[TestOptionalPrimitiveVariableMutationInput]
}

@js.native
trait TestOptionalPrimitiveVariableMutation extends js.Object {
  val primitiveVariable: TestOptionalPrimitiveVariableMutation.PrimitiveVariable
}

object TestOptionalPrimitiveVariableMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestOptionalPrimitiveVariableMutationInput, TestOptionalPrimitiveVariableMutation] {
  type Ctor[T] = T

  @js.native
  trait PrimitiveVariable extends js.Object {
    val id: String
  }

  def newInput(
    a: String | Null = null
  ): _root_.relay.generated.TestOptionalPrimitiveVariableMutationInput =
    _root_.relay.generated.TestOptionalPrimitiveVariableMutationInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalPrimitiveVariableMutation.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
