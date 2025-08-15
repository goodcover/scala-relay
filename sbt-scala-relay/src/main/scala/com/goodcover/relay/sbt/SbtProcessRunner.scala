package com.goodcover.relay.sbt

import com.goodcover.relay.build.{BuildLogger, ProcessRunner}
import com.goodcover.relay.Commands
import sbt.util.Logger

import java.io.{File, InputStream}

/**
 * SBT implementation of ProcessRunner using sbt.Commands
 */
class SbtProcessRunner extends ProcessRunner {
  override def run(
    command: Seq[String],
    workingDir: File,
    logger: BuildLogger,
    outputHandler: InputStream => Unit
  ): Either[String, Unit] = {
    // Use the existing Commands.run implementation from the original RelayCompiler
    val sbtLogger = logger.asInstanceOf[SbtBuildLogger].sbtLogger
    Commands.run(command, workingDir, sbtLogger, outputHandler) match {
      case Left(error) => Left(error)
      case Right(_) => Right(())
    }
  }
}

object SbtProcessRunner {
  def apply(): SbtProcessRunner = new SbtProcessRunner()
}
