package com.goodcover.relay.build

import java.io.{File, InputStream}

/**
 * Abstract process runner interface for executing external commands
 */
trait ProcessRunner {
  def run(
    command: Seq[String],
    workingDir: File,
    logger: BuildLogger,
    outputHandler: InputStream => Unit
  ): Either[String, Unit]
}

/**
 * Default implementation using ProcessBuilder
 */
class DefaultProcessRunner extends ProcessRunner {
  override def run(
    command: Seq[String],
    workingDir: File,
    logger: BuildLogger,
    outputHandler: InputStream => Unit
  ): Either[String, Unit] = {
    try {
      logger.debug(s"Executing command: ${command.mkString(" ")}")
      logger.debug(s"Working directory: ${workingDir.getAbsolutePath}")

      // Use scala.sys.process for cleaner process handling
      import scala.sys.process._

      val stdoutBuffer = new StringBuilder
      val stderrBuffer = new StringBuilder

      // Create ProcessLogger to capture both streams
      val processLogger = ProcessLogger(
        stdout => {
          stdoutBuffer.append(stdout).append("\n")
          logger.debug(s"[stdout] $stdout")
        },
        stderr => {
          stderrBuffer.append(stderr).append("\n")
          logger.debug(s"[stderr] $stderr")
        }
      )

      // Run the process and wait for completion
      val processBuilder = Process(command, workingDir)
      val exitCode = processBuilder.run(processLogger).exitValue()

      // Call the original output handler with stdout
      if (stdoutBuffer.nonEmpty) {
        val stdoutStream = new java.io.ByteArrayInputStream(stdoutBuffer.toString.getBytes)
        outputHandler(stdoutStream)
      }

      if (exitCode == 0) {
        Right(())
      } else {
        val errorMsg = if (stderrBuffer.nonEmpty) {
          s"Process exited with code $exitCode. stderr: ${stderrBuffer.toString.trim}"
        } else {
          s"Process exited with code $exitCode"
        }
        Left(errorMsg)
      }
    } catch {
      case e: Exception =>
        logger.debug(s"Exception running command: ${e.getMessage}")
        Left(e.getMessage)
    }
  }
}
