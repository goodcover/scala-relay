package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestPrimitiveVariableMutation($a: String!) {
    primitiveVariable(a: $a) {
        id
    }
}
*/

trait TestPrimitiveVariableMutationInput extends js.Object {
  val a: String
}

object TestPrimitiveVariableMutationInput {
  def apply(
    a: String
  ): TestPrimitiveVariableMutationInput =
    js.Dynamic.literal(
      "a" -> a.asInstanceOf[js.Any]
    ).asInstanceOf[TestPrimitiveVariableMutationInput]
}

@js.native
trait TestPrimitiveVariableMutation extends js.Object {
  val primitiveVariable: TestPrimitiveVariableMutation.PrimitiveVariable
}

object TestPrimitiveVariableMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestPrimitiveVariableMutationInput, TestPrimitiveVariableMutation] {
  type Ctor[T] = T

  @js.native
  trait PrimitiveVariable extends js.Object {
    val id: String
  }

  def newInput(
    a: String
  ): _root_.relay.generated.TestPrimitiveVariableMutationInput =
    _root_.relay.generated.TestPrimitiveVariableMutationInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestPrimitiveVariableMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
