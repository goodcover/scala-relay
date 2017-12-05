package serde.sjs

import scala.scalajs.js

trait Bindable[K, F <: js.Object] {
  def accumulate(p: K, f: F): K
  def focus(p: K): F
}

object Bindable {

  def instance[K, F <: js.Object](f: K => F, acc: (K, F) => K): Bindable[K, F] = new Bindable[K, F] {
    override def accumulate(p: K, f: F) = acc(p, f)
    override def focus(p: K)            = f(p)
  }

  def apply[K, F <: js.Object](implicit bindable: Bindable[K, F]): Bindable[K, F] = bindable

}
