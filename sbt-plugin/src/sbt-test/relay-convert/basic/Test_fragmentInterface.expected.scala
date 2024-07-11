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

object Test_fragmentInterface extends _root_.relay.gql.FragmentTaggedNode[Test_fragmentInterface] {
  type Ctor[T] = T

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("./__generated__/Test_fragmentInterface.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
