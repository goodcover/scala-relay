package com.goodcover.relay

import scala.scalajs.js
import scala.scalajs.js.|
import scala.scalajs.js.|.Evidence

trait UnionImplicits {

  @inline
  private val baseEv: Evidence[js.Any, js.Any] =
    Evidence.base[js.Any]

  @inline
  implicit val unitEv: Evidence[Unit, js.Any] =
    baseEv.asInstanceOf[Evidence[Unit, js.Any]]

  @inline
  implicit val booleanEv: Evidence[Boolean, js.Any] =
    baseEv.asInstanceOf[Evidence[Boolean, js.Any]]

  @inline
  implicit val byteEv: Evidence[Byte, js.Any] =
    baseEv.asInstanceOf[Evidence[Byte, js.Any]]

  @inline
  implicit val shortEv: Evidence[Short, js.Any] =
    baseEv.asInstanceOf[Evidence[Short, js.Any]]

  @inline
  implicit val intEv: Evidence[Int, js.Any] =
    baseEv.asInstanceOf[Evidence[Int, js.Any]]

  @inline
  implicit val floatEv: Evidence[Float, js.Any] =
    baseEv.asInstanceOf[Evidence[Float, js.Any]]

  @inline
  implicit val doubleEv: Evidence[Double, js.Any] =
    baseEv.asInstanceOf[Evidence[Double, js.Any]]

  @inline
  implicit val stringEv: Evidence[String, js.Any] =
    baseEv.asInstanceOf[Evidence[String, js.Any]]

  implicit def mergeUnion[A, B, C](ab: A | B)(implicit evA: Evidence[A, C], evB: Evidence[B, C]): C =
    ab.asInstanceOf[C]

}

object UnionImplicits extends UnionImplicits
