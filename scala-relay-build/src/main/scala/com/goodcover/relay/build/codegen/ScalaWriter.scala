package com.goodcover.relay.build.codegen

import com.goodcover.relay.build.codegen.ScalaWriter.{applyId, Parameter}

import java.io.Writer
import scala.annotation.tailrec
import scala.meta.{Term, Type}
import scala.meta.prettyprinters.XtensionSyntax

class ScalaWriter(writer: Writer) {

  def writeTrait[Field](
    tname: Type.Name,
    parentTraits: Seq[String],
    fields: Iterable[Field],
    jsNative: Boolean,
    indent: String
  )(writeField: Field => Unit): Unit = {
    if (jsNative) {
      writer.write(indent)
      writer.write("@js.native\n")
    }
    writer.write(indent)
    writer.write("trait ")
    writer.write(tname.syntax)
    val parents = if (parentTraits.isEmpty) Seq("js.Object") else parentTraits
    if (parents.nonEmpty) {
      parents match {
        case base +: tail =>
          writer.write(" extends ")
          writer.write(base)
          tail.foreach { parent =>
            writer.write("with ")
            writer.write(parent)
            writer.write(' ')
          }
        case Seq() => ()
      }
    }
    if (fields.nonEmpty) {
      writer.write(" {\n")
      fields.foreach(writeField)
      writer.write(indent)
      writer.write('}')
    }
    writer.write("\n\n")
  }

  def writeField(ename: Term.Name, scalaType: String, initializer: Option[String], indent: String): Unit = {
    writer.write(indent)
    writer.write("val ")
    writer.write(ename.syntax)
    writer.write(": ")
    writer.write(scalaType)
    initializer.foreach { init =>
      writer.write(" = ")
      writer.write(init)
    }
    writer.write('\n')
  }

  def writeJsLiteralApplyMethod(parameters: Seq[Parameter], typeName: String, compact: Boolean, indent: String): Unit = {
    writer.write(indent)
    writer.write("def ")
    writer.write(applyId.syntax)
    writer.write('(')
    writeParameterList(parameters) { (parameter, isLast) =>
      writer.write(parameter.ename.syntax)
      writer.write(": ")
      writer.write(parameter.scalaType)
      parameter.initializer.foreach { init =>
        writer.write(" = ")
        writer.write(init)
      }
      if (!isLast) writer.write(", ")
    }
    writer.write("): ")
    writer.write(typeName)
    writer.write(" = {\n")
    writer.write(indent)
    writer.write("  val __obj = js.Dynamic.literal(")
    writeParameterList(parameters) { (parameter, isLast) =>
      writer.write('"')
      writer.write(parameter.ename.value)
      writer.write('"')
      writer.write(" -> ")
      writer.write(parameter.ename.syntax)
      writer.write(".asInstanceOf[js.Any]")
      if (!isLast) writer.write(", ")
    }
    writer.write(")\n")
    writer.write(indent)
    writer.write("  __obj.asInstanceOf[")
    writer.write(typeName)
    writer.write("]\n")
    writer.write(indent)
    writer.write("}\n")
    if (!compact) writer.write('\n')
  }

  def writeMethod(
    ename: Term.Name,
    parameters: List[Parameter],
    returnType: String,
    indent: String
  )(writeBody: () => Unit): Unit = {
    writer.write(indent)
    writer.write("def ")
    writer.write(ename.syntax)
    writer.write('(')
    foreachParameter(parameters) {
      case (parameter, hasMore) =>
        writer.write(parameter.ename.syntax)
        writer.write(": ")
        writer.write(parameter.scalaType)
        parameter.initializer.foreach { init =>
          writer.write(" = ")
          writer.write(init)
        }
        if (hasMore) writer.write(", ")
    }
    writer.write("): ")
    writer.write(returnType)
    writer.write(" =")
    writeBody()
  }

  def writeProxyMethod(
    ename: Term.Name,
    parameters: List[Parameter],
    returnType: String,
    proxy: String,
    compact: Boolean,
    indent: String
  ): Unit = {
    writeMethod(ename, parameters, returnType, indent) { () =>
      if (parameters.nonEmpty) {
        writer.write("\n  ")
        writer.write(indent)
      } else {
        writer.write(' ')
      }
      writer.write(proxy)
      writer.write('(')
      if (parameters.nonEmpty) {
        writer.write('\n')
        foreachParameter(parameters) {
          case (field, hasMore) =>
            writer.write(indent)
            writer.write("    ")
            writer.write(field.ename.syntax)
            if (hasMore) writer.write(',')
            writer.write('\n')
        }
        writer.write(indent)
        writer.write("  ")
      }
      writer.write(")\n")
      if (!compact) writer.write('\n')
    }
  }

  def foreachParameter[T](parameters: List[T])(f: (T, Boolean) => Unit): Unit = {
    @tailrec
    def loop(parameters: List[T]): Unit = parameters match {
      case Nil => ()
      case field :: Nil =>
        f(field, false)
      case field :: tail =>
        f(field, true)
        loop(tail)
    }

    loop(parameters)
  }

  private def writeParameterList[T](parameters: Seq[T])(f: (T, Boolean) => Unit): Unit = {
    @tailrec
    def loop(parameters: List[T]): Unit = parameters match {
      case Nil => ()
      case field :: Nil =>
        f(field, true)
      case field :: tail =>
        f(field, false)
        loop(tail)
    }

    loop(parameters.toList)
  }
}

object ScalaWriter {

  private val applyId = Term.Name("apply")

  // We use Term.Name so that it quotes any reserved words.
  final case class Parameter(ename: Term.Name, scalaType: String, initializer: Option[String])

}
