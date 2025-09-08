package com.goodcover.relay

import com.goodcover.relay.build.BuildLogger
import sbt.Logger

/**
  * SBT implementation of BuildLogger
  */
class SbtBuildLogger(val sbtLogger: Logger) extends BuildLogger {
  override def debug(message: String): Unit = sbtLogger.debug(message)
  override def info(message: String): Unit  = sbtLogger.info(message)
  override def warn(message: String): Unit  = sbtLogger.warn(message)
  override def error(message: String): Unit = sbtLogger.error(message)
}

object SbtBuildLogger {
  def apply(logger: Logger): SbtBuildLogger = new SbtBuildLogger(logger)
}
