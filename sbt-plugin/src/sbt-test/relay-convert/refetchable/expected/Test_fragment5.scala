package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment5 on ImplicitNode @refetchable(queryName: "ImplicitNodeArgs") {
    name
}
*/

@js.native
trait Test_fragment5 extends js.Object {
  val name: String
}

object Test_fragment5 extends _root_.relay.gql.FragmentRefetchableTaggedNode[Test_fragment5, ImplicitNodeArgsInput, ImplicitNodeArgs] {
  type Ctor[T] = T

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment5.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
