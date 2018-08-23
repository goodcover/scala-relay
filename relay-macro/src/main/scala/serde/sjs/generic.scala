package serde.sjs

import scala.language.experimental.macros
import scala.scalajs.js

object generic {
  def deriveEncoder[A]: Encoder[A] = macro DerivationMacros.encoder[A]
  def deriveDecoder[A]: Decoder[A] = macro DerivationMacros.decoder[A]
  def deriveHoc[K, F <: js.Object, E]: HocEffect[K, F, E] = macro DerivationMacros.hocEffect[K, F, E]
}
