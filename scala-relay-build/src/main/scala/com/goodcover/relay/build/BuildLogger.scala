package com.goodcover.relay.build

/**
 * Abstract logger interface that can be implemented by different build tools
 */
trait BuildLogger {
  def debug(message: String): Unit
  def info(message: String): Unit
  def warn(message: String): Unit
  def error(message: String): Unit
}
