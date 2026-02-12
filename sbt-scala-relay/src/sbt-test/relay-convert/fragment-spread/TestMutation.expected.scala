package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
mutation TestMutation($input: ActorSubscribeInput!) {
    actorSubscribe(input: $input) {
        clientMutationId
        ...Test_payload
        subscribee {
            address {
                ...Test_address
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
    js.Dynamic
      .literal(
        input = input
      )
      .asInstanceOf[TestMutationInput]
}

@js.native
trait TestMutation extends js.Object {
  val actorSubscribe: TestMutation.ActorSubscribe | Null
}

object TestMutation extends _root_.com.goodcover.relay.MutationTaggedNode[TestMutationInput, TestMutation] {
  type Ctor[T] = T

  @js.native
  trait ActorSubscribeSubscribeeAddress extends js.Object

  @js.native
  trait ActorSubscribeSubscribee extends js.Object {
    val address: ActorSubscribeSubscribeeAddress | Null
  }

  @js.native
  trait ActorSubscribe extends js.Object {
    val clientMutationId: String | Null
    val subscribee: ActorSubscribeSubscribee | Null
  }

  implicit class ActorSubscribe2Test_payloadRef(f: ActorSubscribe)
      extends _root_.com.goodcover.relay.CastToFragmentRef[ActorSubscribe, Test_payload](f) {
    def toTest_payload: _root_.com.goodcover.relay.FragmentRef[Test_payload] = castToRef
  }

  implicit class ActorSubscribeSubscribeeAddress2Test_addressRef(f: ActorSubscribeSubscribeeAddress)
      extends _root_.com.goodcover.relay.CastToFragmentRef[ActorSubscribeSubscribeeAddress, Test_address](f) {
    def toTest_address: _root_.com.goodcover.relay.FragmentRef[Test_address] = castToRef
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
