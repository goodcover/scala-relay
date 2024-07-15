package com.goodcover.relay.codegen

import com.goodcover.relay.codegen.ScalaWriter.{applyId, Parameter}

import java.io.Writer
import scala.annotation.tailrec
import scala.meta.{Term, Type}

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
    writeDeclaration(ename, scalaType, initializer)
    writer.write('\n')
  }

  def writeParameter(ename: Term.Name, scalaType: String, initializer: Option[String], indent: String): Unit = {
    writer.write(indent)
    writeDeclaration(ename, scalaType, initializer)
  }

  private def writeDeclaration(ename: Term.Name, scalaType: String, initializer: Option[String]): Unit = {
    writer.write(ename.syntax)
    writer.write(": ")
    writer.write(scalaType)
    initializer.foreach { init =>
      writer.write(" = ")
      writer.write(init)
    }
  }

  def writeJsLiteralApplyMethod(
    parameters: List[Parameter],
    returnType: String,
    compact: Boolean,
    indent: String
  ): Unit = {
    writeMethod(applyId, parameters, returnType, indent) { () =>
      if (parameters.nonEmpty) {
        writer.write("\n  ")
        writer.write(indent)
      } else {
        writer.write(' ')
      }
      writer.write("js.Dynamic.literal(")
      if (parameters.nonEmpty) {
        writer.write('\n')
        foreachParameter(parameters) {
          case (field, hasMore) =>
            writer.write(indent)
            writer.write("""    """")
            writer.write(field.ename.syntax)
            writer.write("""" -> """)
            writer.write(field.ename.syntax)
            // TODO: GC-3153 - We shouldn't need this now that the objects are js.Object.
            writer.write(".asInstanceOf[js.Any]")
            if (hasMore) writer.write(',')
            writer.write('\n')
        }
        writer.write(indent)
        writer.write("  ")
      }
      writer.write(").asInstanceOf[")
      writer.write(returnType)
      writer.write("]\n")
      if (!compact) writer.write('\n')
    }
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

  def writeMethod(ename: Term.Name, parameters: List[Parameter], returnType: String, indent: String)(
    writeBody: () => Unit
  ): Unit = {
    writer.write(indent)
    writer.write("def ")
    writer.write(ename.syntax)
    writer.write('(')
    if (parameters.nonEmpty) {
      writer.write('\n')
      foreachParameter(parameters) {
        case (parameter, hasMore) =>
          writeParameter(parameter.ename, parameter.scalaType, parameter.initializer, indent + "  ")
          if (hasMore) writer.write(',')
          writer.write('\n')
      }
      writer.write(indent)
    }
    writer.write("): ")
    writer.write(returnType)
    writer.write(" =")
    writeBody()
  }

  private def foreachParameter(parameters: List[Parameter])(f: (Parameter, Boolean) => Unit): Unit = {
    @tailrec
    def loop(fields: List[Parameter]): Unit = fields match {
      case Nil => ()
      case field :: Nil =>
        f(field, false)
      case field :: tail =>
        f(field, true)
        loop(tail)
    }

    loop(parameters)
  }
}

object ScalaWriter {

  private val applyId = Term.Name("apply")

  // We use Term.Name so that it quotes any reserved words.
  final case class Parameter(ename: Term.Name, scalaType: String, initializer: Option[String])

}
