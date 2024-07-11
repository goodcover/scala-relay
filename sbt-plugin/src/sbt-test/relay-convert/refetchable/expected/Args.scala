package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
*/

trait ArgsInput extends js.Object {
  val b: String | Null
}

@js.native
trait Args extends js.Object

object Args extends _root_.relay.gql.QueryTaggedNode[ArgsInput, Args] {
  type Ctor[T] = T

  implicit class Args2Test_fragment2Ref(f: Args) extends _root_.relay.gql.CastToFragmentRef[Args, Test_fragment2](f) {
    def toTest_fragment2: _root_.relay.gql.FragmentRef[Test_fragment2] = castToRef
  }

  def newInput(
    b: String | Null = null
  ): ArgsInput =
    js.Dynamic.literal(
      "b" -> b.asInstanceOf[js.Any]
    ).asInstanceOf[ArgsInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated/Args.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
