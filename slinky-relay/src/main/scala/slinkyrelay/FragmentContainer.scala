package slinkyrelay

import relay.gql.TaggedNode
import slinky.core.{ExternalPropsWriterProvider, KeyAndRefAddingStage, ReactComponentClass}
import slinky.readwrite.Writer

import scala.scalajs.js

object FragmentContainer {

  def build[D, P](p: P, component: ReactComponentClass[P], fragmentSpec: Map[String, TaggedNode])(
    implicit pw: ExternalPropsWriterProvider
  ): KeyAndRefAddingStage[D] = {
    import scala.scalajs.js.JSConverters._
    new KeyAndRefAddingStage(
      js.Array(
        Relay.createFragmentContainer(component, fragmentSpec.toJSDictionary),
        pw.asInstanceOf[Writer[P]].write(p)
      )
    )
  }
}
