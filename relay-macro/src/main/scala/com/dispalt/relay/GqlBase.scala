package com.dispalt.relay

import scala.language.implicitConversions
import scala.scalajs.js


@js.native
trait GqlBase extends js.Object

object GqlBase {
  implicit def gqlBase2func(g: GqlBase): js.Function0[GqlBase] = {
    () => g
  }
}
