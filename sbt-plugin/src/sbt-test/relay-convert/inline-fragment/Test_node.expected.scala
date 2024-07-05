package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_node on Node {
  __typename
  ... on Page {
    name
  }
}
*/

@js.native
trait Test_node extends _root_.relay.gql.Introspectable[Test_node]

object Test_node extends _root_.relay.gql.FragmentTaggedNode[Test_node] {
  type Ctor[T] = T

  object __typename {
    @js.native sealed trait Page extends _root_.relay.gql.Introspectable[Test_node.Page]
    @inline def Page: Page = "Page".asInstanceOf[Page]
    @js.native sealed trait `%other` extends _root_.relay.gql.Introspectable[Test_node]
    @inline def `%other`: `%other` = "%other".asInstanceOf[`%other`]
  }

  @js.native
  trait Page extends js.Object {
    val name: String | Null
  }

  implicit class Test_node_Ops(f: Test_node) {
    def asPage: Option[Page] = {
      if (f == null) None
      else if (js.isUndefined(f.__typename)) {
        if (_root_.scala.scalajs.LinkingInfo.developmentMode) {
          _root_.org.scalajs.dom.console.warn("__typename on Test_node is undefined. Did you forget to include it in your graphql definition?")
        }
        None
      }
      else if (f.__typename == __typename.Page) Some(f.asInstanceOf[Page])
      else None
    }
  }

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_node.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
