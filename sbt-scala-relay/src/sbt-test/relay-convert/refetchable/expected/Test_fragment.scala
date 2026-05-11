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

object Test_fragment extends _root_.com.goodcover.relay.FragmentRefetchableTaggedNode[Test_fragment, NoArgsInput, NoArgs] {
  type Ctor[T] = T

  @js.native
  trait Foo extends js.Object {
    val bar: String | Null
  }

  type Query = _root_.com.goodcover.relay.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
