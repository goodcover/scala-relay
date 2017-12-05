package serde.sjs

import scala.scalajs.js

trait Encoder[A] {
  def apply(a: A): js.Any
}

object Encoder {

  def apply[A](implicit encoder: Encoder[A]): Encoder[A] = encoder

  def instance[A](encode: A => js.Any): Encoder[A] = a => encode(a)

  implicit val intEncoder: Encoder[Int]         = instance(identity[Int])
  implicit val stringEncoder: Encoder[String]   = instance(identity[String])
  implicit val booleanEncoder: Encoder[Boolean] = instance(identity[Boolean])

  implicit def optionEncoder[A](implicit encoder: Encoder[A]): Encoder[Option[A]] = {
    instance {
      case Some(a) => encoder(a)
      case None    => null // scalastyle:ignore null
    }
  }

  implicit def listEncoder[A](implicit encoder: Encoder[A]): Encoder[List[A]] = {
    instance { list =>
      js.Array(list.map(encoder.apply): _*)
    }
  }
}
