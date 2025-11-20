package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestListResponseMutation($a: String!) {
    listResponse(a: $a)
}
 */

trait TestListResponseMutationInput extends js.Object {
  val a: String
}

object TestListResponseMutationInput {
  def apply(
    a: String
  ): TestListResponseMutationInput =
    js.Dynamic
      .literal(
        a = a
      )
      .asInstanceOf[TestListResponseMutationInput]
}

@js.native
trait TestListResponseMutation extends js.Object {
  val listResponse: js.Array[String]
}

object TestListResponseMutation
    extends _root_.com.goodcover.relay.MutationTaggedNode[TestListResponseMutationInput, TestListResponseMutation] {
  type Ctor[T] = T

  def newInput(
    a: String
  ): _root_.relay.generated.TestListResponseMutationInput =
    _root_.relay.generated.TestListResponseMutationInput(
      a
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestListResponseMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
