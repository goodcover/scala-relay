package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestQuery {
    viewer {
        actor {
            id
            address {
                city
                country
            }
        }
        allTimezones {
            timezone
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
  trait ViewerActorAddress extends js.Object {
    val city: String | Null
    val country: String | Null
  }

  @js.native
  trait ViewerActor extends js.Object {
    val id: String
    val address: ViewerActorAddress | Null
  }

  @js.native
  trait ViewerAllTimezones extends js.Object {
    val timezone: String | Null
  }

  @js.native
  trait Viewer extends js.Object {
    val actor: ViewerActor | Null
    val allTimezones: js.Array[ViewerAllTimezones | Null] | Null
  }

  def newInput(): _root_.relay.generated.TestQueryInput = _root_.relay.generated.TestQueryInput()

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
