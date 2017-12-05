package serde.sjs

import scala.scalajs.js
import scala.util.Try

trait Decoder[A] {

  def apply(any: js.Any): Decoder.Result[A]

  def emap[B](decode: A => Decoder.Result[B]): Decoder[B] = {
    Decoder.instance { any =>
      for {
        a <- apply(any)
        b <- decode(a)
      } yield b
    }
  }
}

object Decoder {

  type Result[A] = Either[Throwable, A]

  def apply[A](implicit decoder: Decoder[A]): Decoder[A] = decoder

  def instance[A](decode: js.Any => Result[A]): Decoder[A] = any => decode(any)

  implicit val intDecoder: Decoder[Int] = instance { any =>
    // Don't try to catch an UndefinedBehaviorError
    Try(any.asInstanceOf[Int]).toEither // scalastyle:ignore token
  }

  implicit val stringDecoder: Decoder[String] = instance { any =>
    // Don't try to catch an UndefinedBehaviorError
    Try(any.asInstanceOf[String]).toEither // scalastyle:ignore token
  }

  implicit val booleanDecoder: Decoder[Boolean] = instance { any =>
    Try(any.asInstanceOf[Boolean]).toEither // scalastyle:ignore token
  }

  implicit def optionDecoder[A](implicit decoder: Decoder[A]): Decoder[Option[A]] = {
    instance { any =>
      if (!js.isUndefined(any) && any != null) { // scalastyle:ignore null
        decoder(any).map(Some(_))
      } else {
        Right(None)
      }
    }
  }

  implicit def listDecoder[A](implicit decoder: Decoder[A]): Decoder[List[A]] = {
    instance { any =>
      if (js.Array.isArray(any)) {
        any
          .asInstanceOf[js.Array[js.Any]] // scalastyle:ignore token
          .reverse
          .foldLeft(Right(List.empty): Decoder.Result[List[A]]) { (listResult, elementAny) =>
            for {
              list    <- listResult
              element <- decoder(elementAny)
            } yield element :: list
          }
      } else {
        Left(new RuntimeException(s"$any is not an array."))
      }
    }
  }
}
