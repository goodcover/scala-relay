package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment on Viewer {
    changes(first: 10) @connection(key: "Test_fragment_changes") {
        __id
        edges {
            node {
                description
            }
        }
    }
}
*/

@js.native
trait Test_fragment extends js.Object {
  val changes: Test_fragment.Changes
}

object Test_fragment extends _root_.com.goodcover.relay.FragmentTaggedNode[Test_fragment] {
  type Ctor[T] = T

  @js.native
  trait ChangesEdgesNode extends js.Object {
    val description: String
  }

  @js.native
  trait ChangesEdges extends js.Object {
    val node: ChangesEdgesNode
  }

  @js.native
  trait Changes extends js.Object {
    val __id: String
    val edges: js.Array[ChangesEdges]
  }

  type Query = _root_.com.goodcover.relay.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
