package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestOptionalListVariableMutation($as: [String!]) {
    listVariable(as: $as) {
        id
    }
}
*/

trait TestOptionalListVariableMutationInput extends js.Object {
  val as: js.Array[String] | Null
}

object TestOptionalListVariableMutationInput {
  def apply(
    as: js.Array[String] | Null = null
  ): TestOptionalListVariableMutationInput =
    js.Dynamic.literal(
      as = as
    ).asInstanceOf[TestOptionalListVariableMutationInput]
}

@js.native
trait TestOptionalListVariableMutation extends js.Object {
  val listVariable: TestOptionalListVariableMutation.ListVariable
}

object TestOptionalListVariableMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestOptionalListVariableMutationInput, TestOptionalListVariableMutation] {
  type Ctor[T] = T

  @js.native
  trait ListVariable extends js.Object {
    val id: String
  }

  def newInput(
    as: js.Array[String] | Null = null
  ): _root_.relay.generated.TestOptionalListVariableMutationInput =
    _root_.relay.generated.TestOptionalListVariableMutationInput(
      as
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalListVariableMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
