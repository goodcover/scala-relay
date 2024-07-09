package com.dispalt.relay.codegen

import java.io.Writer

class ScalaWriter(writer: Writer) {

  def writeTrait[Field](
    id: String,
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
    writer.write(id)
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

  def writeField(id: String, typeId: String, initializer: Option[String], indent: String): Unit = {
    writer.write(indent)
    writer.write("val ")
    writeDeclaration(id, typeId, initializer)
    writer.write('\n')
  }

  def writeParameter(id: String, typeId: String, initializer: Option[String], indent: String): Unit = {
    writer.write(indent)
    writeDeclaration(id, typeId, initializer)
  }

  private def writeDeclaration(id: String, typeId: String, initializer: Option[String]): Unit = {
    writer.write(id)
    writer.write(": ")
    writer.write(typeId)
    initializer.foreach { init =>
      writer.write(" = ")
      writer.write(init)
    }
  }
}
