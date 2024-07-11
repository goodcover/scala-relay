package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
*/

trait NoArgsInput extends js.Object

@js.native
trait NoArgs extends js.Object

object NoArgs extends _root_.relay.gql.QueryTaggedNode[NoArgsInput, NoArgs] {
  type Ctor[T] = T

  implicit class NoArgs2Test_fragmentRef(f: NoArgs) extends _root_.relay.gql.CastToFragmentRef[NoArgs, Test_fragment](f) {
    def toTest_fragment: _root_.relay.gql.FragmentRef[Test_fragment] = castToRef
  }

  def newInput(): NoArgsInput = js.Dynamic.literal().asInstanceOf[NoArgsInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated/NoArgs.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
