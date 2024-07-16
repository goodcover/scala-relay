package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment2 on Query @refetchable(queryName: "Args") {
    oneArg(a: $b) {
        bar
    }
}
*/

@js.native
trait Test_fragment2 extends js.Object {
  val oneArg: Test_fragment2.OneArg
}

object Test_fragment2 extends _root_.com.goodcover.relay.FragmentRefetchableTaggedNode[Test_fragment2, ArgsInput, Args] {
  type Ctor[T] = T

  @js.native
  trait OneArg extends js.Object {
    val bar: String | Null
  }

  type Query = _root_.com.goodcover.relay.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment2.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
