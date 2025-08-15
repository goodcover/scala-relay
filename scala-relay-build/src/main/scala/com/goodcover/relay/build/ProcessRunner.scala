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
      val processBuilder = new ProcessBuilder(command: _*)
      processBuilder.directory(workingDir)
      val process = processBuilder.start()
      
      outputHandler(process.getInputStream)
      
      val exitCode = process.waitFor()
      if (exitCode == 0) {
        Right(())
      } else {
        Left(s"Process exited with code $exitCode")
      }
    } catch {
      case e: Exception => Left(e.getMessage)
    }
  }
}
