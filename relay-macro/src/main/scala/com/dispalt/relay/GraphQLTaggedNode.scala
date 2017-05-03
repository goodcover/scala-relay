package com.dispalt.relay

import scala.language.implicitConversions
import scala.scalajs.js

trait GenericGraphQLTaggedNode {
  val raw: js.Any
}

object GenericGraphQLTaggedNode {
  implicit def ggql2jsObj(ggqltn: GenericGraphQLTaggedNode): GraphQLTaggedNode = {
    ggqltn.raw.asInstanceOf[GraphQLTaggedNode]
  }
}

@js.native
trait GraphQLTaggedNode extends js.Object

@js.native
trait GraphQLTaggedFragment extends GraphQLTaggedNode

@js.native
trait GraphQLTaggedQuery extends GraphQLTaggedNode

object GraphQLTaggedNode {
  type Func = js.Function0[GraphQLTaggedNode]

  implicit def gqlBase2func(g: GraphQLTaggedNode): Func = {
    () => g
  }
}

object GraphQLTaggedFragment {
  type Func = js.Function0[GraphQLTaggedFragment]

  implicit def gqlBase2func(g: GraphQLTaggedFragment): Func = {
    () => g
  }
}

object GraphQLTaggedQuery {
  type Func = js.Function0[GraphQLTaggedQuery]

  implicit def gqlBase2func(g: GraphQLTaggedQuery): Func = {
    () => g
  }
}
