package com.goodcover.relay

import scala.scalajs.js

@js.native
trait Introspectable[A] extends js.Object {
  val __typename: Introspectable.TypeName[A]
}

object Introspectable {

  @js.native
  trait TypeName[A] extends js.Any

  def as[A <: Introspectable[A], B <: A](f: A, typeName: TypeName[B]): Option[B] =
    if (f == null) None
    else if (f.__typename == typeName) Some(f.asInstanceOf[B])
    else None
}
