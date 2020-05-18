package slinkyrelay

import relay.gql.GenericGraphQLTaggedNode

trait HasRefetch {

  val refetchQuery: GenericGraphQLTaggedNode
}
