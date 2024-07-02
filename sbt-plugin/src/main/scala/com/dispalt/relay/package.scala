package com.dispalt

import sbt.File

package object relay {

  /**
    * Inverts a map and ensures that it was a lossless conversion.
    */
  def invertFiles(map: Map[File, File]): Map[File, File] = {
    val inverse = map.map {
      case (from, to) => to -> from
    }
    if (inverse.size != map.size) {
      raiseError(map.toSeq)
    }
    inverse
  }

  /**
    * Inverts a map and ensures that it was a lossless conversion.
    */
  def invertFilesMulti(map: Map[File, Iterable[File]]): Map[File, File] = {
    val inverse = map.flatMap {
      case (from, to) => to.map(_ -> from)
    }
    val mapValueCount = map.values.foldLeft(0)((count, tos) => count + tos.size)
    if (inverse.size != mapValueCount) {
      raiseError(map.toSeq.flatMap {
        case (from, tos) => tos.map(from -> _)
      })
    }
    inverse
  }

  private def raiseError(entries: Seq[(File, File)]) = {
    entries.groupBy(_._2).find(_._2.length > 2) match {
      case Some((to, entries)) =>
        val froms = entries.map(_._1).mkString(", ")
        throw new IllegalArgumentException(s"Found multiple files ($froms) that map to $to.")
      case None =>
        throw new IllegalStateException("BUG: Inverse has fewer entries than original but failed to find the file with multiple mappings.")
    }
  }
}
