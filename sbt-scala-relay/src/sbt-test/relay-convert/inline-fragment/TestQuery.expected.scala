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

object TestQueryInput {
  def apply(): TestQueryInput = js.Dynamic.literal().asInstanceOf[TestQueryInput]
}

@js.native
trait TestQuery extends js.Object {
  val maybeNode: TestQuery.MaybeNode | Null
}

object TestQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestQueryInput, TestQuery] {
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
  trait MaybeNode extends _root_.com.goodcover.relay.Introspectable[MaybeNode]

  object MaybeNode {
    object __typename {
      @js.native sealed trait Story extends _root_.com.goodcover.relay.Introspectable.TypeName[TestQuery.MaybeNodeStory]
      @inline def Story: Story = "Story".asInstanceOf[Story]
      @js.native sealed trait `%other` extends _root_.com.goodcover.relay.Introspectable.TypeName[TestQuery.MaybeNode]
      @inline def `%other`: `%other` = "%other".asInstanceOf[`%other`]
    }
  }

  implicit class MaybeNode_Ops(f: MaybeNode) {
    def asStory: Option[MaybeNodeStory] = _root_.com.goodcover.relay.Introspectable.as(f, MaybeNode.__typename.Story)
  }

  def newInput(): _root_.relay.generated.TestQueryInput = _root_.relay.generated.TestQueryInput()

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
