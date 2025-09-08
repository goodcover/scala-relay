package com.goodcover.relay.build

import caliban.parsing.adt.{Definition, Document}
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.{FieldDefinition, InputValueDefinition}
import caliban.rendering.DocumentRenderer
import com.goodcover.relay.build.GraphQLSchema.FieldTypeDefinition

import java.io.{ByteArrayInputStream, ObjectInputStream}
import java.util.Base64
import scala.collection.Seq
import scala.util.Using

package object codegen {

  private[codegen] type ArgLookup       = String => Option[InputValueDefinition]
  private[codegen] type FieldLookup     = String => Option[FieldDefinition]
  private[codegen] type FieldTypeLookup = String => Option[FieldTypeDefinition]

  def renderDefinition(definition: Definition, document: Document): String =
    trimBlankLines(DocumentRenderer.render(Document(List(definition), document.sourceMapper)))

  def invertOneToOne[A, B](map: Map[A, B]): Map[B, Vector[A]] =
    map.foldLeft(Map.empty[B, Vector[A]]) {
      case (acc, (a, b)) => acc.updated(b, acc.get(b).fold(Vector(a))(_ :+ a))
    }

  def invertOneToMany[A, B](map: Map[A, Iterable[B]]): Map[B, Vector[A]] =
    map.foldLeft(Map.empty[B, Vector[A]]) {
      case (acc, (a, bs)) =>
        bs.foldLeft(acc) { (acc, b) =>
          acc.updated(b, acc.get(b).fold(Vector(a))(_ :+ a))
        }
    }

//  private def deserializeFromString[A <: Serializable](s: String): A = {
//    val data = Base64.getDecoder.decode(s)
//    Using.wrap(new ByteArrayInputStream(_: Array[Byte])).apply(data) { byteArrayInputStream =>
//      Using.resource(new ObjectInputStream(_))(byteArrayInputStream) { objectInputStream =>
//        objectInputStream.readObject().asInstanceOf[A]
//      }
//    }
//  }

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
}
