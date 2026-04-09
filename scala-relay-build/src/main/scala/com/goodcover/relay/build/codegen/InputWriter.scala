package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.InputObjectTypeDefinition
import caliban.parsing.adt.Type.innerType
import caliban.parsing.adt.{ Directive, Document, Type }
import com.goodcover.relay.build.codegen.ScalaWriter.Parameter

import java.io.Writer
import scala.meta.Term

class InputWriter(writer: Writer, input: InputObjectTypeDefinition, document: Document, typeConverter: TypeConverter)
    extends DefinitionWriter(writer) {

  // TODO: Deduplicate.
  private val parameters = input.fields.map { field =>
    val tpe         = field.ofType
    val typeName    = innerType(tpe)
    val scalaType   = typeConverter.convertToScalaType(tpe, typeName, field.directives, fullyQualified = false)
    // TODO: Default value.
    val initializer = if (tpe.nonNull) None else Some("null")
    Parameter(Term.Name(field.name), scalaType, initializer)
  }

  override protected def definitionName: String = input.name

  override protected def writeImports(): Unit = {
    writer.write("import _root_.scala.scalajs.js\n")
    writer.write("import _root_.scala.scalajs.js.|\n\n")
  }

  override protected def writeDefinitionText(): Unit = {
    writer.write(renderDefinition(input, document))
    writer.write('\n')
  }

  override def write(): Unit = {
    writePreamble()
    writeInputType(compact = true)
  }

  def writeInputType(compact: Boolean = false): Unit = {
    scalaWriter.writeTrait(meta.Type.Name(definitionName), Seq.empty, input.fields, jsNative = false, "") { field =>
      writeInputField(field.name, field.ofType, field.directives)
    }
    writeInputCompanionObject(compact)
  }

  private def writeInputField(name: String, tpe: Type, fieldDefinitionDirectives: List[Directive]): Unit = {
    val typeName  = innerType(tpe)
    val scalaType = typeConverter.convertToScalaType(tpe, typeName, fieldDefinitionDirectives, fullyQualified = false)
    scalaWriter.writeField(Term.Name(name), scalaType, None, "  ")
  }

  private def writeInputCompanionObject(compact: Boolean): Unit = {
    writer.write("object ")
    writer.write(definitionName)
    writer.write(" {\n")
    writeInputApplyMethod()
    writer.write("}\n")
    if (!compact) writer.write('\n')
  }

  private def writeInputApplyMethod(): Unit =
    scalaWriter.writeJsLiteralApplyMethod(parameters, definitionName, compact = true, "  ")
}
