package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_node on Node {
    __typename
    ... on User {
        name
    }
    ... on Page {
        name
        actor {
            __typename
            id
            ... on User {
                name
                ...Test_user
            }
        }
    }
}
*/

@js.native
trait Test_node extends _root_.relay.gql.Introspectable[Test_node]

object Test_node extends _root_.relay.gql.FragmentTaggedNode[Test_node] {
  type Ctor[T] = T

  object __typename {
    @js.native sealed trait User extends _root_.relay.gql.Introspectable[Test_node.User]
    @inline def User: User = "User".asInstanceOf[User]
    @js.native sealed trait Page extends _root_.relay.gql.Introspectable[Test_node.Page]
    @inline def Page: Page = "Page".asInstanceOf[Page]
    @js.native sealed trait `%other` extends _root_.relay.gql.Introspectable[Test_node]
    @inline def `%other`: `%other` = "%other".asInstanceOf[`%other`]
  }

  @js.native
  trait User extends js.Object {
    val name: String | Null
  }

  @js.native
  trait PageActorUser extends js.Object {
    val name: String | Null
  }

  @js.native
  trait PageActor extends _root_.relay.gql.Introspectable[PageActor] {
    val id: String
  }

  object PageActor {
    object __typename {
      @js.native sealed trait User extends _root_.relay.gql.Introspectable[Test_node.PageActorUser]
      @inline def User: User = "User".asInstanceOf[User]
      @js.native sealed trait `%other` extends _root_.relay.gql.Introspectable[Test_node.PageActor]
      @inline def `%other`: `%other` = "%other".asInstanceOf[`%other`]
    }

  }

  @js.native
  trait Page extends js.Object {
    val name: String | Null
    val actor: PageActor | Null
  }

  implicit class PageActorUser2Test_userRef(f: PageActorUser) extends _root_.relay.gql.CastToFragmentRef[PageActorUser, Test_user](f) {
    def toTest_user: _root_.relay.gql.FragmentRef[Test_user] = castToRef
  }

  implicit class PageActor_Ops(f: PageActor) {
    def asUser: Option[PageActorUser] = _root_.relay.gql.Introspectable.as(f, PageActor.__typename.User)
  }

  implicit class Test_node_Ops(f: Test_node) {
    def asUser: Option[User] = _root_.relay.gql.Introspectable.as(f, Test_node.__typename.User)
    def asPage: Option[Page] = _root_.relay.gql.Introspectable.as(f, Test_node.__typename.Page)
  }

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_node.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
