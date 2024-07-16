package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
*/

trait ViewerQueryInput extends js.Object

@js.native
trait ViewerQuery extends js.Object

object ViewerQuery extends _root_.com.goodcover.relay.QueryTaggedNode[ViewerQueryInput, ViewerQuery] {
  type Ctor[T] = T

  implicit class ViewerQuery2Test_viewerRef(f: ViewerQuery) extends _root_.com.goodcover.relay.CastToFragmentRef[ViewerQuery, Test_viewer](f) {
    def toTest_viewer: _root_.com.goodcover.relay.FragmentRef[Test_viewer] = castToRef
  }

  def newInput(): ViewerQueryInput = js.Dynamic.literal().asInstanceOf[ViewerQueryInput]

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/ViewerQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
