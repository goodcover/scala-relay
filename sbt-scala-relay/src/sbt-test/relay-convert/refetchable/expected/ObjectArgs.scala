package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
*/

trait ObjectArgsInput extends js.Object {
  val thing: js.UndefOr[Thing | Null]
}

object ObjectArgsInput {
  def apply(
    thing: js.UndefOr[Thing | Null] = js.undefined
  ): ObjectArgsInput =
    js.Dynamic.literal(
      thing = thing
    ).asInstanceOf[ObjectArgsInput]
}

@js.native
trait ObjectArgs extends js.Object

object ObjectArgs extends _root_.com.goodcover.relay.QueryTaggedNode[ObjectArgsInput, ObjectArgs] {
  type Ctor[T] = T

  implicit class ObjectArgs2Test_fragment4Ref(f: ObjectArgs) extends _root_.com.goodcover.relay.CastToFragmentRef[ObjectArgs, Test_fragment4](f) {
    def toTest_fragment4: _root_.com.goodcover.relay.FragmentRef[Test_fragment4] = castToRef
  }

  def newInput(
    thing: js.UndefOr[_root_.relay.generated.Thing | Null] = js.undefined
  ): _root_.relay.generated.ObjectArgsInput =
    _root_.relay.generated.ObjectArgsInput(
      thing
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/ObjectArgs.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
