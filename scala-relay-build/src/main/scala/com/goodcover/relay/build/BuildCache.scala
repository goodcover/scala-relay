package com.goodcover.relay.build

import java.io.File

/**
 * Abstract cache interface that can be implemented by different build tools
 */
trait BuildCache {
  def clean(): Unit
}

/**
 * Abstract cache factory interface
 */
trait BuildCacheFactory {
  def apply(name: String): BuildCache
}

/**
 * File information for caching
 */
case class FileInfo(file: File, lastModified: Long)

object FileInfo {
  def lastModified(file: File): FileInfo = FileInfo(file, file.lastModified())
}

/**
 * Results from build operations
 */
case class BuildResults(outputs: Set[File])
