package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment6 on Query @refetchable(queryName: "ArgsOfSpread") {
    foo {
        args {
            ...Test_args
        }
    }
}
*/

@js.native
trait Test_fragment6 extends js.Object {
  val foo: Test_fragment6.Foo
}

object Test_fragment6 extends _root_.relay.gql.FragmentRefetchableTaggedNode[Test_fragment6, ArgsOfSpreadInput, ArgsOfSpread] {
  type Ctor[T] = T

  @js.native
  trait FooArgs extends js.Object

  @js.native
  trait Foo extends js.Object {
    val args: FooArgs | Null
  }

  implicit class FooArgs2Test_argsRef(f: FooArgs) extends _root_.relay.gql.CastToFragmentRef[FooArgs, Test_args](f) {
    def toTest_args: _root_.relay.gql.FragmentRef[Test_args] = castToRef
  }

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment6.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
