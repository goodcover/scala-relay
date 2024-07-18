package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
*/

trait ImplicitNodeArgsInput extends js.Object {
  val id: String
}

object ImplicitNodeArgsInput {
  def apply(
    id: String
  ): ImplicitNodeArgsInput =
    js.Dynamic.literal(
      id = id
    ).asInstanceOf[ImplicitNodeArgsInput]
}

@js.native
trait ImplicitNodeArgs extends js.Object

object ImplicitNodeArgs extends _root_.com.goodcover.relay.QueryTaggedNode[ImplicitNodeArgsInput, ImplicitNodeArgs] {
  type Ctor[T] = T

  implicit class ImplicitNodeArgs2Test_fragment5Ref(f: ImplicitNodeArgs) extends _root_.com.goodcover.relay.CastToFragmentRef[ImplicitNodeArgs, Test_fragment5](f) {
    def toTest_fragment5: _root_.com.goodcover.relay.FragmentRef[Test_fragment5] = castToRef
  }

  def newInput(
    id: String
  ): _root_.relay.generated.ImplicitNodeArgsInput =
    _root_.relay.generated.ImplicitNodeArgsInput(
      id
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/ImplicitNodeArgs.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
