package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment on Foo {
    bar @scalajs(extends: "ThingamyWhatsit") {
        baz
    }
}
*/

@js.native
trait Test_fragment extends js.Object {
  val bar: Test_fragment.Bar
}

object Test_fragment extends _root_.com.goodcover.relay.FragmentTaggedNode[Test_fragment] {
  type Ctor[T] = T

  @js.native
  trait Bar extends ThingamyWhatsit {
    val baz: String
  }

  type Query = _root_.com.goodcover.relay.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
