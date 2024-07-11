package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestNoVariablesMutation {
    noVariables {
        clientMutationId
    }
}
*/

trait TestNoVariablesMutationInput extends js.Object

@js.native
trait TestNoVariablesMutation extends js.Object {
  val noVariables: TestNoVariablesMutation.NoVariables | Null
}

object TestNoVariablesMutation extends _root_.relay.gql.MutationTaggedNode[TestNoVariablesMutationInput, TestNoVariablesMutation] {
  type Ctor[T] = T

  @js.native
  trait NoVariables extends js.Object {
    val clientMutationId: String | Null
  }

  def newInput(): TestNoVariablesMutationInput = js.Dynamic.literal().asInstanceOf[TestNoVariablesMutationInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated/TestNoVariablesMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
