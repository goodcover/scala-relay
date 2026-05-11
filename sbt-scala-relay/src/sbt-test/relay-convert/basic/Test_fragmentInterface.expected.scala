package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragmentInterface on Node {
    name
}
*/

@js.native
trait Test_fragmentInterface extends js.Object {
  val name: String | Null
}

object Test_fragmentInterface extends _root_.com.goodcover.relay.FragmentTaggedNode[Test_fragmentInterface] {
  type Ctor[T] = T

  type Query = _root_.com.goodcover.relay.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragmentInterface.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
