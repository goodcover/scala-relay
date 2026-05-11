package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_viewer on Viewer @refetchable(queryName: "ViewerQuery") {
    name
}
*/

@js.native
trait Test_viewer extends js.Object {
  val name: String
}

object Test_viewer extends _root_.com.goodcover.relay.FragmentRefetchableTaggedNode[Test_viewer, ViewerQueryInput, ViewerQuery] {
  type Ctor[T] = T

  type Query = _root_.com.goodcover.relay.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_viewer.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
