package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment on Viewer {
    actor {
        id
        address {
            city
            country
        }
    }
}
*/

@js.native
trait Test_fragment extends js.Object {
  val actor: Test_fragment.Actor | Null
}

object Test_fragment extends _root_.com.goodcover.relay.FragmentTaggedNode[Test_fragment] {
  type Ctor[T] = T

  @js.native
  trait ActorAddress extends js.Object {
    val city: String | Null
    val country: String | Null
  }

  @js.native
  trait Actor extends js.Object {
    val id: String
    val address: ActorAddress | Null
  }

  type Query = _root_.com.goodcover.relay.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
