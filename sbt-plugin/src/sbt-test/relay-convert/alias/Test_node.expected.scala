package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_node on Node {
    no: __typename
    moniker: name
    content: body {
        words: text
        ...Test_text
    }
    ... on User {
        surname: lastName
        msg: message {
            txt: text
        }
    }
}
*/

@js.native
trait Test_node extends _root_.relay.gql.Introspectable[Test_node] {
  val moniker: String | Null
  val content: Test_node.Content | Null
}

object Test_node extends _root_.relay.gql.FragmentTaggedNode[Test_node] {
  type Ctor[T] = T

  object __typename {
    @js.native sealed trait User extends _root_.relay.gql.Introspectable[Test_node.User]
    @inline def User: User = "User".asInstanceOf[User]
    @js.native sealed trait `%other` extends _root_.relay.gql.Introspectable[Test_node]
    @inline def `%other`: `%other` = "%other".asInstanceOf[`%other`]
  }

  @js.native
  trait Content extends js.Object {
    val words: String | Null
  }

  @js.native
  trait UserMsg extends js.Object {
    val txt: String | Null
  }

  @js.native
  trait User extends js.Object {
    val surname: String | Null
    val msg: UserMsg | Null
  }

  implicit class Content2Test_textRef(f: Content) extends _root_.relay.gql.CastToFragmentRef[Content, Test_text](f) {
    def toTest_text: _root_.relay.gql.FragmentRef[Test_text] = castToRef
  }

  implicit class Test_node_Ops(f: Test_node) {
    def asUser: Option[User] = _root_.relay.gql.Introspectable.as(f, Test_node.__typename.User)
  }

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_node.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
