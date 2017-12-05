package serde.sjs

import scala.language.experimental.macros
import scala.scalajs.js

object generic {
  def deriveEncoder[A]: Encoder[A] = macro DerivationMacros.encoder[A]
  def deriveDecoder[A]: Decoder[A] = macro DerivationMacros.decoder[A]
  def deriveBindable[K, F <: js.Object]: Bindable[K, F] = macro DerivationMacros.bindable[K, F]
}
