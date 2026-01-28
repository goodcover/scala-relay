package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.{ InputObjectTypeDefinition, InputValueDefinition }
import caliban.parsing.adt.Document
import caliban.parsing.adt.Type.innerType
import com.goodcover.relay.build.codegen.OperationInputWriter.newInputName
import com.goodcover.relay.build.codegen.ScalaWriter.Parameter

import java.io.Writer
import scala.meta.Term

class OperationInputWriter(
  writer: Writer,
  operation: OperationDefinition,
  document: Document,
  typeConverter: TypeConverter
) {

  private val scalaWriter: ScalaWriter = new ScalaWriter(writer)

  private val operationName: String = DocumentConverter.getOperationName(operation)

  val operationInputName: String = operationName + "Input"

  // TODO: Deduplicate.
  private val parameters = operation.variableDefinitions.map { variable =>
    val tpe         = variable.variableType
    val typeName    = innerType(tpe)
    val scalaType   = typeConverter.convertToScalaType(tpe, typeName, variable.directives, fullyQualified = true)
    // TODO: Default value.
    val initializer = if (tpe.nonNull) None else Some("null")
    Parameter(Term.Name(variable.name), scalaType, initializer)
  }

  def writeOperationInputType(): Unit = {
    val fields = operation.variableDefinitions.map { variable =>
      InputValueDefinition(None, variable.name, variable.variableType, variable.defaultValue, variable.directives)
    }
    val input  = InputObjectTypeDefinition(None, operationInputName, operation.directives, fields)
    new InputWriter(writer, input, document, typeConverter).writeInputType()
  }

  def writeNewInputMethod(): Unit = {
    val fullyQualifiedName = "_root_.relay.generated." + operationInputName
    scalaWriter.writeProxyMethod(
      newInputName,
      parameters,
      fullyQualifiedName,
      fullyQualifiedName,
      compact = false,
      "  "
    )
  }
}

object OperationInputWriter {

  private val newInputName = Term.Name("newInput")
}
