package com.dispalt

import sbt.util.CacheImplicits
import sjsonnew.*

import scala.annotation.tailrec

package object relay {

  // Workaround for https://github.com/sbt/sbt/issues/7594.
  implicit def mapFormat[K, V](
    implicit keyFormat: JsonKeyFormat[K],
    valueFormat: JsonFormat[V]
  ): RootJsonFormat[Map[K, V]] =
    new RootJsonFormat[Map[K, V]] {
      private val delegate = CacheImplicits.mapFormat[K, V]
      override def read[J](jsOpt: Option[J], unbuilder: Unbuilder[J]): Map[K, V] = {
        require(unbuilder.state == UnbuilderState.InObject)
        @tailrec
        def loop(acc: Map[K, V]): Map[K, V] = {
          if (unbuilder.hasNextField) {
            val (k, v) = unbuilder.nextFieldOpt()
            loop(acc.updated(keyFormat.read(k), valueFormat.read(v, unbuilder)))
          } else acc
        }
        val result = loop(Map.empty)
        unbuilder.endObject()
        result
      }
      override def write[J](obj: Map[K, V], builder: Builder[J]): Unit =
        delegate.write(obj, builder)
    }

  /**
    * Inverts a map and ensures that it was a lossless conversion.
    */
  def invertOneToOneOrThrow[A, B](map: Map[A, B]): Map[B, A] = {
    val inverse = map.map {
      case (a, b) => b -> a
    }
    if (inverse.size != map.size) {
      raiseError(map.toSeq)
    }
    inverse
  }

  /**
    * Inverts a map and ensures that it was a lossless conversion.
    */
  def invertOneToManyOrThrow[A, B](map: Map[A, Iterable[B]]): Map[B, A] = {
    val inverse = map.flatMap {
      case (a, b) => b.map(_ -> a)
    }
    val mapValueCount = map.values.foldLeft(0)((count, bs) => count + bs.size)
    if (inverse.size != mapValueCount) {
      raiseError(map.toSeq.flatMap {
        case (a, bs) => bs.map(a -> _)
      })
    }
    inverse
  }

  private def raiseError[A, B](entries: Seq[(A, B)]) = {
    entries.groupBy(_._2).find(_._2.length > 1) match {
      case Some((b, aToBs)) =>
        val as = aToBs.map(_._1).mkString(", ")
        throw new IllegalArgumentException(s"Found multiple values ($as) that map to $b.")
      case None =>
        throw new IllegalStateException(
          "BUG: Inverse has fewer entries than original but failed to find the value with multiple mappings."
        )
    }
  }

  def invertOneToOne[A, B](map: Map[A, B]): Map[B, Iterable[A]] =
    map.foldLeft(Map.empty[B, Vector[A]]) {
      case (acc, (a, b)) => acc.updated(b, acc.get(b).fold(Vector(a))(_ :+ a))
    }

  def invertOneToMany[A, B](map: Map[A, Iterable[B]]): Map[B, Iterable[A]] =
    map.foldLeft(Map.empty[B, Vector[A]]) {
      case (acc, (a, bs)) =>
        bs.foldLeft(acc) { (acc, b) =>
          acc.updated(b, acc.get(b).fold(Vector(a))(_ :+ a))
        }
    }
}
