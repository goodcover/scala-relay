package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestMutation($input: ActorSubscribeInput!) {
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

trait TestMutationInput extends js.Object {
  val input: ActorSubscribeInput
}

object TestMutationInput {
  def apply(
    input: ActorSubscribeInput
  ): TestMutationInput =
    js.Dynamic.literal(
      input = input
    ).asInstanceOf[TestMutationInput]
}

@js.native
trait TestMutation extends js.Object {
  val actorSubscribe: TestMutation.ActorSubscribe | Null
}

object TestMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestMutationInput, TestMutation] {
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
    input: _root_.relay.generated.ActorSubscribeInput
  ): _root_.relay.generated.TestMutationInput =
    _root_.relay.generated.TestMutationInput(
      input
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestMutation.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
