package relay.gql

import scala.annotation.unchecked.uncheckedVariance
import scala.scalajs.js
import scala.scalajs.js.|

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

/** Relay fragment definition for response of type Ctor[Out]. */
trait FragmentTaggedNode[O] extends GenericGraphQLTaggedNode {
  type Query <: TaggedNodeQuery[Ctor, O, Any]

  /** Query in/out constructor. Either `js.Array[T]` or `T`. */
  type Ctor[T]

  type Out = O
  type Ref = FragmentRef[O]

  override def query: Query
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

abstract class CastToFragmentRef[From, To](from: From) {
  def castToRef: FragmentRef[To] = from.asInstanceOf[FragmentRef[To]]
}

/** This is what all the Relay components request */
trait TaggedNode extends js.Object

@js.native
trait ConcreteFragment extends TaggedNode

@js.native
trait ConcreteBatch extends TaggedNode

@js.native
trait ConcreteRequest extends TaggedNode

/** Typed fragment query.
  *
  * @tparam F fragment value type constructor (`js.Array` for plural fragments, otherwise `Id`).
  * @tparam O fragment value type.
  * @tparam X inline indicator (`Inline` for inline fragments, otherwise `Any`).
  */
@js.native
trait TaggedNodeQuery[F[_], O, +X] extends TaggedNode

trait Inline
