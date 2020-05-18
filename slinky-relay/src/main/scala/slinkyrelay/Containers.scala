package slinkyrelay

import relay.gql.TaggedNode
import slinky.core.{ExternalPropsWriterProvider, KeyAndRefAddingStage, ReactComponentClass}
import slinky.readwrite.Writer

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

object Containers {

  def buildFragmentContainer[D, P](p: P, component: ReactComponentClass[P], fragmentSpec: Map[String, TaggedNode])(
    implicit pw: ExternalPropsWriterProvider
  ): KeyAndRefAddingStage[D] = {
    new KeyAndRefAddingStage(
      js.Array(
        Relay.createFragmentContainer(component, fragmentSpec.toJSDictionary),
        pw.asInstanceOf[Writer[P]].write(p)
      )
    )
  }

  def buildRefetchContainer[D, P](
    p: P,
    component: ReactComponentClass[P],
    fragmentSpec: Map[String, TaggedNode],
    refetchQuery: TaggedNode
  )(implicit pw: ExternalPropsWriterProvider): KeyAndRefAddingStage[D] = {
    new KeyAndRefAddingStage(
      js.Array(
        Relay.createRefetchContainer(component, fragmentSpec.toJSDictionary, refetchQuery),
        pw.asInstanceOf[Writer[P]].write(p)
      )
    )
  }
}
