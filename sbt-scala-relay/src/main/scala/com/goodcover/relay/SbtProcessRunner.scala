package com.goodcover.relay

import com.goodcover.relay.build.{BuildLogger, Commands, ProcessRunner}

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
    Commands.run(command, workingDir, logger, outputHandler) match {
      case Left(error) => Left(error)
      case Right(_)    => Right(())
    }
  }
}

object SbtProcessRunner {
  def apply(): SbtProcessRunner = new SbtProcessRunner()
}
