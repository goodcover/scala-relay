package serde.sjs

import scala.scalajs.js

trait HocEffect[K, F <: js.Object, E] {
  type Effect = E
  def effect[EC](f: F)(implicit e: E => EC): EC = f.asInstanceOf[EC]
  def accumulate(p: K, f: F): K
  def focus(p: K): F
}

object HocEffect {
  def instance[K, F <: js.Object, E](f: K => F, acc: (K, F) => K): HocEffect[K, F, E] = new HocEffect[K, F, E] {
    override def accumulate(p: K, f: F) = acc(p, f)
    override def focus(p: K)            = f(p)
  }

  def instanceNone[K, F <: js.Object](f: K => F, acc: (K, F) => K): HocEffect[K, F, Unit] = new HocEffect[K, F, Unit] {
    override def accumulate(p: K, f: F) = acc(p, f)
    override def focus(p: K)            = f(p)
  }

  def apply[K, F <: js.Object, E](implicit effect: HocEffect[K, F, E]): HocEffect[K, F, E] = effect

}
