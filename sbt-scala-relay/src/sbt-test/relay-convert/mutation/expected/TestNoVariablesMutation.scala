package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestNoVariablesMutation {
    noVariables {
        id
    }
}
*/

trait TestNoVariablesMutationInput extends js.Object

object TestNoVariablesMutationInput {
  def apply(): TestNoVariablesMutationInput = js.Dynamic.literal().asInstanceOf[TestNoVariablesMutationInput]
}

@js.native
trait TestNoVariablesMutation extends js.Object {
  val noVariables: TestNoVariablesMutation.NoVariables
}

object TestNoVariablesMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestNoVariablesMutationInput, TestNoVariablesMutation] {
  type Ctor[T] = T

  @js.native
  trait NoVariables extends js.Object {
    val id: String
  }

  def newInput(): _root_.relay.generated.TestNoVariablesMutationInput = _root_.relay.generated.TestNoVariablesMutationInput()

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestNoVariablesMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
