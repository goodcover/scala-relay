package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment4 on Query @refetchable(queryName: "ObjectArgs") {
    objArg(thing: $thing) {
        bar
    }
}
*/

@js.native
trait Test_fragment4 extends js.Object {
  val objArg: Test_fragment4.ObjArg
}

object Test_fragment4 extends _root_.com.goodcover.relay.FragmentRefetchableTaggedNode[Test_fragment4, ObjectArgsInput, ObjectArgs] {
  type Ctor[T] = T

  @js.native
  trait ObjArg extends js.Object {
    val bar: String | Null
  }

  type Query = _root_.com.goodcover.relay.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment4.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
