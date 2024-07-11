package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
*/

trait ArgsOfSpreadInput extends js.Object {
  val b: String | Null
}

@js.native
trait ArgsOfSpread extends js.Object

object ArgsOfSpread extends _root_.relay.gql.QueryTaggedNode[ArgsOfSpreadInput, ArgsOfSpread] {
  type Ctor[T] = T

  implicit class ArgsOfSpread2Test_fragment6Ref(f: ArgsOfSpread) extends _root_.relay.gql.CastToFragmentRef[ArgsOfSpread, Test_fragment6](f) {
    def toTest_fragment6: _root_.relay.gql.FragmentRef[Test_fragment6] = castToRef
  }

  def newInput(
    b: String | Null = null
  ): ArgsOfSpreadInput =
    js.Dynamic.literal(
      "b" -> b.asInstanceOf[js.Any]
    ).asInstanceOf[ArgsOfSpreadInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("./__generated__/ArgsOfSpread.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
