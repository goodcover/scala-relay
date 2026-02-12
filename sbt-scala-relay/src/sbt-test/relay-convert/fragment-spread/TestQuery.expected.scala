package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestQuery {
    viewer {
        ...Test_viewer
        actor {
            address {
                ...Test_address
            }
        }
    }
}
 */

trait TestQueryInput extends js.Object

object TestQueryInput {
  def apply(): TestQueryInput = js.Dynamic.literal().asInstanceOf[TestQueryInput]
}

@js.native
trait TestQuery extends js.Object {
  val viewer: TestQuery.Viewer | Null
}

object TestQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestQueryInput, TestQuery] {
  type Ctor[T] = T

  @js.native
  trait ViewerActorAddress extends js.Object

  @js.native
  trait ViewerActor extends js.Object {
    val address: ViewerActorAddress | Null
  }

  @js.native
  trait Viewer extends js.Object {
    val actor: ViewerActor | Null
  }

  implicit class Viewer2Test_viewerRef(f: Viewer) extends _root_.com.goodcover.relay.CastToFragmentRef[Viewer, Test_viewer](f) {
    def toTest_viewer: _root_.com.goodcover.relay.FragmentRef[Test_viewer] = castToRef
  }

  implicit class ViewerActorAddress2Test_addressRef(f: ViewerActorAddress)
      extends _root_.com.goodcover.relay.CastToFragmentRef[ViewerActorAddress, Test_address](f) {
    def toTest_address: _root_.com.goodcover.relay.FragmentRef[Test_address] = castToRef
  }

  def newInput(): _root_.relay.generated.TestQueryInput = _root_.relay.generated.TestQueryInput()

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
