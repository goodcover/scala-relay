package slinkyrelay

import relay.gql.TaggedNode
import slinky.core.ReactComponentClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

// TODO: Move to facade package
@JSImport("react-relay", JSImport.Namespace)
@js.native
private object Relay extends js.Object {

  def createFragmentContainer[P](
    component: ReactComponentClass[P],
    fragmentSpec: js.Dictionary[TaggedNode]
  ): ReactComponentClass[P] =
    js.native

  def createRefetchContainer[P](
    component: ReactComponentClass[P],
    fragmentSpec: js.Dictionary[TaggedNode],
    refetchQuery: TaggedNode
  ): ReactComponentClass[P] =
    js.native
}
