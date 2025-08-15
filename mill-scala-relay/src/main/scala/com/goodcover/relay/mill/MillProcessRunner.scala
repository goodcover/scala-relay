package com.goodcover.relay.mill

import com.goodcover.relay.build.{BuildLogger, ProcessRunner}
import mill.javalib.api.JvmWorkerApi.Ctx

import java.io.{File, InputStream}
import scala.util.Try

/**
  * Mill implementation of ProcessRunner using Mill's process execution
  */
class MillProcessRunner extends ProcessRunner {
  override def run(
    command: Seq[String],
    workingDir: File,
    logger: BuildLogger,
    outputHandler: InputStream => Unit
  ): Either[String, Unit] = {
    try {
      logger.info(s"Running command: ${command.mkString(" ")}")
      logger.debug(s"Working directory: $workingDir")

      val processBuilder = new ProcessBuilder(command*)
      processBuilder.directory(workingDir)

      val process = processBuilder.start()

      // Handle output
      outputHandler(process.getInputStream)

      val exitCode = process.waitFor()
      if (exitCode == 0) {
        Right(())
      } else {
        Left(s"Process exited with code $exitCode")
      }
    } catch {
      case e: Exception =>
        logger.error(s"Failed to run command: ${e.getMessage}")
        Left(e.getMessage)
    }
  }
}

object MillProcessRunner {
  def apply(): MillProcessRunner = new MillProcessRunner()
}
