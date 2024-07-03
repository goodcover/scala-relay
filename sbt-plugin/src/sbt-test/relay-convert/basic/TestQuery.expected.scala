package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestQuery {
  defaultSettings {
    notificationSounds
    cache_id
  }
}
*/

trait TestQueryInput extends js.Object

trait TestQuery extends js.Object {
  val defaultSettings: TestQuery.DefaultSettings | Null
}

object TestQuery extends _root_.relay.gql.QueryTaggedNode[TestQueryInput, TestQuery] {
  type Ctor[T] = T

  trait DefaultSettings extends js.Object {
    val notificationSounds: Boolean | Null
    val cache_id: String | Null
  }

  def newInput(): TestQueryInput = js.Dynamic.literal().asInstanceOf[TestQueryInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
