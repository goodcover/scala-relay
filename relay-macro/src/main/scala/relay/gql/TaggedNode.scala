package relay.gql

import scala.language.implicitConversions
import scala.scalajs.js

/** This is one level higher than what's returned by relay, the query is what's returned by `Relay` */
trait GenericGraphQLTaggedNode {
  def query: TaggedNode
}

/** The typed version */
trait TypedGraphQLTaggedNode[I <: js.Object, O <: js.Object] extends GenericGraphQLTaggedNode

object GenericGraphQLTaggedNode {
  implicit def ggql2jsObj(ggqltn: GenericGraphQLTaggedNode): TaggedNode = {
    ggqltn.query
  }
}

/** Query */
trait QueryTaggedNode[I <: js.Object, O <: js.Object] extends TypedGraphQLTaggedNode[I, O] {
  type Input = I
  type Out   = O
}

object QueryTaggedNode {
  implicit def ggql2jsObj[I <: js.Object, O <: js.Object](ggqltn: QueryTaggedNode[I, O]): TaggedNode = {
    ggqltn.query
  }
}

/** Mutation */
trait MutationTaggedNode[I <: js.Object, O <: js.Object] extends TypedGraphQLTaggedNode[I, O] {
  type Input = I
  type Out   = O
}

object MutationTaggedNode {
  implicit def ggql2jsObj[I <: js.Object, O <: js.Object](ggqltn: MutationTaggedNode[I, O]): TaggedNode = {
    ggqltn.query
  }
}

/** Subscription */
trait SubscriptionTaggedNode[I <: js.Object, O <: js.Object] extends TypedGraphQLTaggedNode[I, O] {
  type Input = I
  type Out   = O
}

object SubscriptionTaggedNode {
  implicit def ggql2jsObj[I <: js.Object, O <: js.Object](ggqltn: SubscriptionTaggedNode[I, O]): TaggedNode = {
    ggqltn.query
  }
}

trait FragmentTaggedNode[O <: js.Object] extends GenericGraphQLTaggedNode {
  type Out = O
}

object FragmentTaggedNode {
  implicit def fragmentTaggedNodeConv[O <: js.Object](ftn: FragmentTaggedNode[O]): TaggedNode = {
    ftn.query
  }
}

trait FragmentRefetchableTaggedNode[O <: js.Object, RI <: js.Object, RO <: js.Object] extends FragmentTaggedNode[O] {
  type RefetchIn  = RI
  type RefetchOut = RO
}

object FragmentRefetchableTaggedNode {
  implicit def fragmentTaggedNodeConv[O <: js.Object, RI <: js.Object, RO <: js.Object](
    ftn: FragmentRefetchableTaggedNode[O, RI, RO]
  ): TaggedNode = {
    ftn.query
  }
}

/** This is what all the Relay components request */
trait TaggedNode extends js.Object

@js.native
trait ConcreteFragment extends TaggedNode

@js.native
trait ConcreteBatch extends TaggedNode

@js.native
trait ReaderFragment extends TaggedNode

@js.native
trait ConcreteRequest extends TaggedNode
