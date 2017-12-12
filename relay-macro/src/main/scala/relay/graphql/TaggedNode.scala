package relay.graphql

import scala.language.implicitConversions
import scala.scalajs.js

trait GenericGraphQLTaggedNode {
  val query: TaggedNode
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

object TaggedNode {
  type Func = js.Function0[TaggedNode]

  implicit def gqlBase2func(g: TaggedNode): Func = () => g
}

object ConcreteFragment {
  type Func = js.Function0[ConcreteFragment]

  implicit def gqlBase2func(g: ConcreteFragment): Func = () => g
}

object ConcreteBatch {
  type Func = js.Function0[ConcreteBatch]

  implicit def gqlBase2func(g: ConcreteBatch): Func = () => g
}
