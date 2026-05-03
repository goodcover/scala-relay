package com.goodcover.relay.mill

import com.goodcover.relay.build.BuildLogger
import mill.api.Logger

/**
 * Mill implementation of BuildLogger
 */
class MillBuildLogger(logger: Logger) extends BuildLogger {
  override def debug(message: String): Unit = logger.debug(message)
  override def info(message: String): Unit  = logger.info(message)
  override def warn(message: String): Unit  = logger.error(message) // Mill doesn't have warn, use error
  override def error(message: String): Unit = logger.error(message)
}

object MillBuildLogger {
  def apply(logger: Logger): MillBuildLogger = new MillBuildLogger(logger)
}
