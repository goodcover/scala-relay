package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment on Settings {
  ...Test_fragment2
}
*/

trait Test_fragment extends js.Object {
}

object Test_fragment extends _root_.relay.gql.FragmentTaggedNode[Test_fragment] {
  type Ctor[T] = T

  implicit class Test_fragment2Test_fragment2Ref(f: Test_fragment) extends _root_.relay.gql.CastToFragmentRef[Test_fragment, Test_fragment2](f) {
    def toTest_fragment2: _root_.relay.gql.FragmentRef[Test_fragment2] = castToRef
  }

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
