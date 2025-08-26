package com.goodcover.relay.build

import java.io._
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.Files

// File operations helper
object FileOps {
  implicit class FileOps2(private val file: File) {
    def /(child: String): File = new File(file, child)

    /** The last component of this path. */
    def name: String = file.getName

    def baseAndExt: (String, String) = {
      val nme = name
      val dot = nme.lastIndexOf('.')
      if (dot < 0) (nme, "") else (nme.substring(0, dot), nme.substring(dot + 1))
    }

    /**
      * The extension part of the name of this path.
      * This is the part of the name after the last period, or the empty string if there is no period.
      */
    def ext: String = baseAndExt._2

    /**
      * The base of the name of this path.
      * This is the part of the name before the last period, or the full name if there is no period.
      */
    def base: String = baseAndExt._1
  }

  private def closeCloseable[T <: AutoCloseable]: T => Unit = _.close()

  def file[T <: AutoCloseable](openF: File => T): OpenFile[T] = file(openF, closeCloseable)

  def file[T](openF: File => T, closeF: T => Unit): OpenFile[T] =
    new OpenFile[T] {
      def openImpl(file: File) = openF(file)
      def close(t: T)          = closeF(t)
    }

  def fileWriter(charset: Charset = StandardCharsets.UTF_8, append: Boolean = false) =
    file(f => new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, append), charset)))

  def fileReader(charset: Charset) =
    file(f => new BufferedReader(new InputStreamReader(new FileInputStream(f), charset)))

  trait OpenFile[T] extends Using[File, T] {
    protected def openImpl(file: File): T
    protected final def open(file: File): T = {
      val parent = file.getParentFile
      if (parent != null) {
        try Files.createDirectory(parent.toPath)
        catch { case _: IOException => }
      }
      openImpl(file)
    }
  }

  abstract class Using[Source, T] {
    protected def open(src: Source): T
    def apply[R](src: Source)(f: T => R): R = {
      val resource = open(src)
      try {
        f(resource)
      } finally {
        close(resource)
      }
    }
    protected def close(out: T): Unit
  }
}
