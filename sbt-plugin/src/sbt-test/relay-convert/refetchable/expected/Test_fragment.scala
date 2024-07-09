package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment on Query @refetchable(queryName: "NoArgs") {
    foo {
        bar
    }
}
*/

@js.native
trait Test_fragment extends js.Object {
  val foo: Test_fragment.Foo
}

object Test_fragment extends _root_.relay.gql.FragmentRefetchableTaggedNode[Test_fragment, NoArgsInput, NoArgs] {
  type Ctor[T] = T

  @js.native
  trait Foo extends js.Object {
    val bar: String | Null
  }

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
