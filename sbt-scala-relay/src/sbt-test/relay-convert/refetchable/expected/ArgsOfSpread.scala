package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
*/

trait ArgsOfSpreadInput extends js.Object {
  val b: js.UndefOr[String | Null]
}

object ArgsOfSpreadInput {
  def apply(
    b: js.UndefOr[String | Null] = js.undefined
  ): ArgsOfSpreadInput =
    js.Dynamic.literal(
      b = b
    ).asInstanceOf[ArgsOfSpreadInput]
}

@js.native
trait ArgsOfSpread extends js.Object

object ArgsOfSpread extends _root_.com.goodcover.relay.QueryTaggedNode[ArgsOfSpreadInput, ArgsOfSpread] {
  type Ctor[T] = T

  implicit class ArgsOfSpread2Test_fragment6Ref(f: ArgsOfSpread) extends _root_.com.goodcover.relay.CastToFragmentRef[ArgsOfSpread, Test_fragment6](f) {
    def toTest_fragment6: _root_.com.goodcover.relay.FragmentRef[Test_fragment6] = castToRef
  }

  def newInput(
    b: js.UndefOr[String | Null] = js.undefined
  ): _root_.relay.generated.ArgsOfSpreadInput =
    _root_.relay.generated.ArgsOfSpreadInput(
      b
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/ArgsOfSpread.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
