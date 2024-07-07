package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestOptionalObjectVariableMutation($input: ActorSubscribeInput) {
    actorSubscribe(input: $input) {
        clientMutationId
    }
}
*/

trait TestOptionalObjectVariableMutationInput extends js.Object {
  val clientMutationId: String | Null
  val subscribeeId: String | Null
}

@js.native
trait TestOptionalObjectVariableMutation extends js.Object {
  val actorSubscribe: TestOptionalObjectVariableMutation.ActorSubscribe | Null
}

object TestOptionalObjectVariableMutation extends _root_.relay.gql.MutationTaggedNode[TestOptionalObjectVariableMutationInput, TestOptionalObjectVariableMutation] {
  type Ctor[T] = T

  @js.native
  trait ActorSubscribe extends js.Object {
    val clientMutationId: String | Null
  }

  def newInput(
    clientMutationId: String | Null = null,
    subscribeeId: String | Null = null
  ): TestOptionalObjectVariableMutationInput =
    js.Dynamic.literal(
      "clientMutationId" -> clientMutationId.asInstanceOf[js.Any],
      "subscribeeId" -> subscribeeId.asInstanceOf[js.Any]
    ).asInstanceOf[TestOptionalObjectVariableMutationInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestOptionalObjectVariableMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
