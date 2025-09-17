package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
 */

trait NoArgsInput extends js.Object

object NoArgsInput {
  def apply(): NoArgsInput = js.Dynamic.literal().asInstanceOf[NoArgsInput]
}

@js.native
trait NoArgs extends js.Object

object NoArgs extends _root_.com.goodcover.relay.QueryTaggedNode[NoArgsInput, NoArgs] {
  type Ctor[T] = T

  implicit class NoArgs2Test_fragmentRef(f: NoArgs)
      extends _root_.com.goodcover.relay.CastToFragmentRef[NoArgs, Test_fragment](f) {
    def toTest_fragment: _root_.com.goodcover.relay.FragmentRef[Test_fragment] = castToRef
  }

  def newInput(): _root_.relay.generated.NoArgsInput = _root_.relay.generated.NoArgsInput()

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/NoArgs.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
