package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_task on Task {
  title
}
*/

trait Test_task extends js.Object {
  val title: String | Null
}

object Test_task extends _root_.relay.gql.FragmentTaggedNode[Test_task] {
  type Ctor[T] = T

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_task.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
