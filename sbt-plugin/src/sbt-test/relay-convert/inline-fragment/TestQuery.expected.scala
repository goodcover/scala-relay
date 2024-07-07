package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestQuery {
    maybeNode {
        __typename
        ... on Story {
            author {
                lastName
            }
        }
    }
}
*/

trait TestQueryInput extends js.Object

@js.native
trait TestQuery extends js.Object {
  val maybeNode: TestQuery.MaybeNode | Null
}

object TestQuery extends _root_.relay.gql.QueryTaggedNode[TestQueryInput, TestQuery] {
  type Ctor[T] = T

  @js.native
  trait MaybeNodeStoryAuthor extends js.Object {
    val lastName: String | Null
  }

  @js.native
  trait MaybeNodeStory extends MaybeNode {
    val author: MaybeNodeStoryAuthor | Null
  }

  @js.native
  trait MaybeNode extends _root_.relay.gql.Introspectable[MaybeNode]

  object MaybeNode {
    object __typename {
      @js.native sealed trait Story extends _root_.relay.gql.Introspectable.TypeName[TestQuery.MaybeNodeStory]
      @inline def Story: Story = "Story".asInstanceOf[Story]
      @js.native sealed trait `%other` extends _root_.relay.gql.Introspectable.TypeName[TestQuery.MaybeNode]
      @inline def `%other`: `%other` = "%other".asInstanceOf[`%other`]
    }

  }

  implicit class MaybeNode_Ops(f: MaybeNode) {
    def asStory: Option[MaybeNodeStory] = _root_.relay.gql.Introspectable.as(f, MaybeNode.__typename.Story)
  }

  def newInput(): TestQueryInput = js.Dynamic.literal().asInstanceOf[TestQueryInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
