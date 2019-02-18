package relay.graphql

import scala.language.implicitConversions
import scala.scalajs.js

trait GenericGraphQLTaggedNode {
  def query: TaggedNode
}

object GenericGraphQLTaggedNode {
  implicit def ggql2jsObj(ggqltn: GenericGraphQLTaggedNode): TaggedNode = {
    ggqltn.query
  }
}

trait QueryTaggedNode[I <: js.Object, O <: js.Object] extends GenericGraphQLTaggedNode {
  type Input = I
  type Out   = O
}

object QueryTaggedNode {
  implicit def ggql2jsObj[I <: js.Object, O <: js.Object](ggqltn: QueryTaggedNode[I, O]): TaggedNode = {
    ggqltn.query
  }
}

trait MutationTaggedNode[I <: js.Object, O <: js.Object] extends GenericGraphQLTaggedNode {
  type Input = I
  type Out   = O
}

object MutationTaggedNode {
  implicit def ggql2jsObj[I <: js.Object, O <: js.Object](ggqltn: MutationTaggedNode[I, O]): TaggedNode = {
    ggqltn.query
  }
}

trait TaggedNode extends js.Object

@js.native
trait ConcreteFragment extends TaggedNode

@js.native
trait ConcreteBatch extends TaggedNode

@js.native
trait ReaderFragment extends TaggedNode
