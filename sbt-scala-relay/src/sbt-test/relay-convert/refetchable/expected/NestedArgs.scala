package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
*/

trait NestedArgsInput extends js.Object {
  val b: String | Null
}

@js.native
trait NestedArgs extends js.Object

object NestedArgs extends _root_.com.goodcover.relay.QueryTaggedNode[NestedArgsInput, NestedArgs] {
  type Ctor[T] = T

  implicit class NestedArgs2Test_fragment3Ref(f: NestedArgs) extends _root_.com.goodcover.relay.CastToFragmentRef[NestedArgs, Test_fragment3](f) {
    def toTest_fragment3: _root_.com.goodcover.relay.FragmentRef[Test_fragment3] = castToRef
  }

  def newInput(
    b: String | Null = null
  ): NestedArgsInput =
    js.Dynamic.literal(
      "b" -> b.asInstanceOf[js.Any]
    ).asInstanceOf[NestedArgsInput]

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/NestedArgs.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
