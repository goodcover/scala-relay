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

object Test_viewer extends _root_.relay.gql.FragmentRefetchableTaggedNode[Test_viewer, ViewerQueryInput, ViewerQuery] {
  type Ctor[T] = T

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated/Test_viewer.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
