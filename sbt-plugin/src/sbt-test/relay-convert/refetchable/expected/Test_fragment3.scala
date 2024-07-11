package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment3 on Query @refetchable(queryName: "NestedArgs") {
    foo {
        args {
            oneArg(a: $b) {
                bar
            }
        }
    }
}
*/

@js.native
trait Test_fragment3 extends js.Object {
  val foo: Test_fragment3.Foo
}

object Test_fragment3 extends _root_.relay.gql.FragmentRefetchableTaggedNode[Test_fragment3, NestedArgsInput, NestedArgs] {
  type Ctor[T] = T

  @js.native
  trait FooArgsOneArg extends js.Object {
    val bar: String | Null
  }

  @js.native
  trait FooArgs extends js.Object {
    val oneArg: FooArgsOneArg
  }

  @js.native
  trait Foo extends js.Object {
    val args: FooArgs | Null
  }

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("./__generated__/Test_fragment3.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
