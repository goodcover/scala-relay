package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestListVariableMutation($as: [String!]!) {
    listVariable(as: $as) {
        id
    }
}
*/

trait TestListVariableMutationInput extends js.Object {
  val as: js.Array[String]
}

object TestListVariableMutationInput {
  def apply(
    as: js.Array[String]
  ): TestListVariableMutationInput =
    js.Dynamic.literal(
      as = as
    ).asInstanceOf[TestListVariableMutationInput]
}

@js.native
trait TestListVariableMutation extends js.Object {
  val listVariable: TestListVariableMutation.ListVariable
}

object TestListVariableMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestListVariableMutationInput, TestListVariableMutation] {
  type Ctor[T] = T

  @js.native
  trait ListVariable extends js.Object {
    val id: String
  }

  def newInput(
    as: js.Array[String]
  ): _root_.relay.generated.TestListVariableMutationInput =
    _root_.relay.generated.TestListVariableMutationInput(
      as
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestListVariableMutation.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
