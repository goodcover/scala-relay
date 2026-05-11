package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
*/

trait ObjectArgsInput extends js.Object {
  val thing: Thing | Null
}

object ObjectArgsInput {
  def apply(
    thing: Thing | Null = null
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
    thing: _root_.relay.generated.Thing | Null = null
  ): _root_.relay.generated.ObjectArgsInput =
    _root_.relay.generated.ObjectArgsInput(
      thing
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/ObjectArgs.graphql", JSImport.Default)
  private object __relayGeneratedNode extends js.Object

  lazy val query: Query = __relayGeneratedNode.asInstanceOf[Query]

  lazy val sourceHash: String = __relayGeneratedNode.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
