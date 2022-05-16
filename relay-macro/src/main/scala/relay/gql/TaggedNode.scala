package relay.gql

import scala.scalajs.js

/** This is one level higher than what's returned by relay, the query is what's returned by `Relay` */
trait GenericGraphQLTaggedNode {
  def query: TaggedNode
}

/**
  * The typed version.
  *
  * We use phantom types here to signal input (`I`) and output (`O`)
  *
  */
trait TypedGraphQLTaggedNode[I, O] extends GenericGraphQLTaggedNode

object GenericGraphQLTaggedNode {
  implicit def ggql2jsObj(ggqltn: GenericGraphQLTaggedNode): TaggedNode = {
    ggqltn.query
  }
}

/** Query */
trait QueryTaggedNode[I, O] extends TypedGraphQLTaggedNode[I, O] {
  type Input = I
  type Out   = O
}

object QueryTaggedNode {
  implicit def ggql2jsObj[I, O](ggqltn: QueryTaggedNode[I, O]): TaggedNode = {
    ggqltn.query
  }
}

/** Mutation */
trait MutationTaggedNode[I, O] extends TypedGraphQLTaggedNode[I, O] {
  type Input = I
  type Out   = O
}

object MutationTaggedNode {
  implicit def ggql2jsObj[I, O](ggqltn: MutationTaggedNode[I, O]): TaggedNode = {
    ggqltn.query
  }
}

/** Subscription */
trait SubscriptionTaggedNode[I, O] extends TypedGraphQLTaggedNode[I, O] {
  type Input = I
  type Out   = O
}

object SubscriptionTaggedNode {
  implicit def ggql2jsObj[I, O](ggqltn: SubscriptionTaggedNode[I, O]): TaggedNode = {
    ggqltn.query
  }
}

trait FragmentTaggedNode[O] extends GenericGraphQLTaggedNode {
  type Out = O
  type Ref = FragmentRef[O]
}

object FragmentTaggedNode {
  implicit def fragmentTaggedNodeConv[O](ftn: FragmentTaggedNode[O]): TaggedNode = {
    ftn.query
  }
}

trait FragmentRefetchableTaggedNode[O, RI, RO] extends FragmentTaggedNode[O] {
  type RefetchIn  = RI
  type RefetchOut = RO
}

object FragmentRefetchableTaggedNode {
  implicit def fragmentTaggedNodeConv[O, RI, RO](ftn: FragmentRefetchableTaggedNode[O, RI, RO]): TaggedNode = {
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
