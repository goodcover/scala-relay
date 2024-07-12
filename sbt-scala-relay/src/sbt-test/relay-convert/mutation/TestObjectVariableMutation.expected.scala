package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestObjectVariableMutation($input: ActorSubscribeInput!) {
    actorSubscribe(input: $input) {
        clientMutationId
        subscribee {
            id
            address {
                city
                country
            }
        }
    }
}
*/

trait TestObjectVariableMutationInput extends js.Object {
  val clientMutationId: String | Null
  val subscribeeId: String | Null
}

@js.native
trait TestObjectVariableMutation extends js.Object {
  val actorSubscribe: TestObjectVariableMutation.ActorSubscribe | Null
}

object TestObjectVariableMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestObjectVariableMutationInput, TestObjectVariableMutation] {
  type Ctor[T] = T

  @js.native
  trait ActorSubscribeSubscribeeAddress extends js.Object {
    val city: String | Null
    val country: String | Null
  }

  @js.native
  trait ActorSubscribeSubscribee extends js.Object {
    val id: String
    val address: ActorSubscribeSubscribeeAddress | Null
  }

  @js.native
  trait ActorSubscribe extends js.Object {
    val clientMutationId: String | Null
    val subscribee: ActorSubscribeSubscribee | Null
  }

  def newInput(
    clientMutationId: String | Null = null,
    subscribeeId: String | Null = null
  ): TestObjectVariableMutationInput =
    js.Dynamic.literal(
      "clientMutationId" -> clientMutationId.asInstanceOf[js.Any],
      "subscribeeId" -> subscribeeId.asInstanceOf[js.Any]
    ).asInstanceOf[TestObjectVariableMutationInput]

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestObjectVariableMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
