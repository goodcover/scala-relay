package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment on Node @inline {
    name
}
*/

@js.native
trait Test_fragment extends js.Object {
  val name: String
}

object Test_fragment extends _root_.com.goodcover.relay.FragmentTaggedNode[Test_fragment] {
  type Ctor[T] = T

  type Query = _root_.com.goodcover.relay.ReaderInlineDataFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
