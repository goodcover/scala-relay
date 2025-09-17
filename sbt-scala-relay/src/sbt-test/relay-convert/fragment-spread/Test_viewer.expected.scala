package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_viewer on Viewer {
    ...Test_viewer2
    actor {
        address {
            ...Test_address
        }
    }
}
 */

@js.native
trait Test_viewer extends js.Object {
  val actor: Test_viewer.Actor | Null
}

object Test_viewer extends _root_.com.goodcover.relay.FragmentTaggedNode[Test_viewer] {
  type Ctor[T] = T

  @js.native
  trait ActorAddress extends js.Object

  @js.native
  trait Actor extends js.Object {
    val address: ActorAddress | Null
  }

  implicit class Test_viewer2Test_viewer2Ref(f: Test_viewer)
      extends _root_.com.goodcover.relay.CastToFragmentRef[Test_viewer, Test_viewer2](f) {
    def toTest_viewer2: _root_.com.goodcover.relay.FragmentRef[Test_viewer2] = castToRef
  }

  implicit class ActorAddress2Test_addressRef(f: ActorAddress)
      extends _root_.com.goodcover.relay.CastToFragmentRef[ActorAddress, Test_address](f) {
    def toTest_address: _root_.com.goodcover.relay.FragmentRef[Test_address] = castToRef
  }

  type Query = _root_.com.goodcover.relay.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_viewer.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
