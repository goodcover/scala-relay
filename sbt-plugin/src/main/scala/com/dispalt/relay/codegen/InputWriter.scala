package com.dispalt.relay.codegen

import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.{InputObjectTypeDefinition, InputValueDefinition}
import caliban.parsing.adt.Type.innerType
import caliban.parsing.adt.{Directive, Type}
import com.dispalt.relay.GraphQLSchema

import java.io.Writer
import scala.annotation.tailrec

// TODO: This should not be specific to an operation but be shared instead.
class InputWriter(
                   writer: Writer,
                   scalaWriter: ScalaWriter,
                   typeConverter: TypeConverter,
                   operation: OperationDefinition,
                   schema: GraphQLSchema
) {

  private val operationName: String = DocumentWriter.getOperationName(operation)

  // TODO: Don't do this. It only works if there is a single input object.
  private def operationSingleInputObjectFields() = {
    operation.variableDefinitions match {
      case variable :: Nil =>
        schema.inputObjectTypes.get(innerType(variable.variableType)) match {
          case Some(_) if variable.defaultValue.nonEmpty =>
            // TODO: We could support this by providing a default object somewhere
            throw new UnsupportedOperationException("A single input object variable cannot have a default value.")
          case Some(obj) => obj.fields
          case None      => Nil
        }
      case _ => Nil
    }
  }

  // TODO: Don't do this. We should create the input types from the schema and share them.
  def writeOperationInputTypes(): Unit = {
    @tailrec
    def loop(remaining: Set[String], done: Set[String]): Unit = {
      remaining.headOption match {
        case Some(typeName) =>
          val nextDone = done + typeName
          schema.inputObjectTypes.get(typeName) match {
            case Some(input) =>
              writeInputType(input)
              val nextRemaining = remaining.tail ++ input.fields
                .map(field => innerType(field.ofType))
                .filterNot(done.contains)
              loop(nextRemaining, nextDone)
            case None =>
              loop(remaining.tail, nextDone)
          }
        case None => ()
      }
    }

    writeOperationInputType()

    operation.variableDefinitions match {
      case variable :: Nil =>
        schema.inputObjectTypes.get(innerType(variable.variableType)).foreach { input =>
          // FIXME: If the input type is cyclic then we will end up with two distinct types. It's not a big deal and we
          //  probably have no instances of that anyway. It would be annoying to prevent and pointless since I want to
          //  change how all these input types are generated anyway.
          loop(input.fields.map(field => innerType(field.ofType)).toSet, Set.empty)
        }
      case _ => ()
    }
  }

  // TODO: Don't do this. We should create the input types from the schema and share them.
  private def writeOperationInputType(): Unit = {
    val singleInputObjectFields = operationSingleInputObjectFields()
    // FIXME: Core uses an empty object instead of null.
    //if (singleInputObjectFields.nonEmpty) {
    scalaWriter.writeTrait(operationName + "Input", Seq.empty, singleInputObjectFields, jsNative = false, "") { field =>
      writeInputField(
        field.name,
        field.ofType,
        hasSelections = schema.inputObjectTypes.contains(innerType(field.ofType)),
        field.directives
      )
    }
    //}
  }

  private def writeInputType(input: InputObjectTypeDefinition): Unit = {
    // FIXME: Core uses an empty object instead of null.
    scalaWriter.writeTrait(operationName + input.name, Seq.empty, input.fields, jsNative = false, "") { field =>
      writeInputField(
        field.name,
        field.ofType,
        hasSelections = schema.inputObjectTypes.contains(innerType(field.ofType)),
        field.directives
      )
    }
    writeInputCompanionObject(input)
  }

  private def writeInputCompanionObject(input: InputObjectTypeDefinition): Unit = {
    writer.write("object ")
    writer.write(operationName + input.name)
    writer.write(" {\n")
    writeInputApplyMethod(input)
    writer.write("}\n\n")
  }

  private def writeInputApplyMethod(input: InputObjectTypeDefinition): Unit = {
    writeInputFactoryMethod("apply", input.fields, operationName + input.name)
  }

  def writeNewInputMethod(): Unit = {
    val fields     = operationSingleInputObjectFields()
    val returnType = operationName + "Input"
    writeInputFactoryMethod("newInput", fields, returnType)
  }

  private def writeInputFactoryMethod(
    name: String,
    parameters: List[InputValueDefinition],
    returnType: String
  ): Unit = {
    // TODO: This ought to delegate to an apply method on the input object.

    // FIXME: Core uses an empty object instead of null.

    def foreachInputField(f: (InputValueDefinition, Boolean) => Unit): Unit = {
      @tailrec
      def loop(fields: List[InputValueDefinition]): Unit = fields match {
        case Nil => ()
        case field :: Nil =>
          f(field, false)
        case field :: tail =>
          f(field, true)
          loop(tail)
      }

      loop(parameters)
    }

    writer.write("  def ")
    writer.write(name)
    writer.write('(')
    if (parameters.nonEmpty) {
      writer.write('\n')
      foreachInputField {
        case (field, hasMore) =>
          writeInputFieldParameter(field.name, field.ofType, field.directives)
          if (hasMore) writer.write(',')
          writer.write('\n')
      }
      writer.write("  ")
    }
    writer.write("): ")
    writer.write(returnType)
    writer.write(" =")
    if (parameters.nonEmpty) {
      writer.write("\n    ")
    } else {
      writer.write(' ')
    }
    writer.write("js.Dynamic.literal(")
    if (parameters.nonEmpty) {
      writer.write('\n')
      foreachInputField {
        case (field, hasMore) =>
          writer.write("""      """")
          writer.write(field.name)
          writer.write("""" -> """)
          writer.write(field.name)
          // TODO: We shouldn't need this now that the objects are js.Object.
          writer.write(".asInstanceOf[js.Any]")
          if (hasMore) writer.write(',')
          writer.write('\n')
      }
      writer.write("    ")
    }
    writer.write(").asInstanceOf[")
    writer.write(returnType)
    writer.write("]\n")
    //}
  }

  private def writeInputFieldParameter(name: String, tpe: Type, directives: List[Directive]): Unit = {
    // TODO: This typeName stuff is weird.
    val typeName     = innerType(tpe)
    val fullTypeName = if (schema.inputObjectTypes.contains(typeName)) operationName + typeName else typeName
    val scalaTypeId  = typeConverter.convertToScalaType(tpe, fullTypeName, directives)
    val initializer  = if (tpe.nonNull) None else Some("null")
    scalaWriter.writeParameter(name, scalaTypeId, initializer, "    ")
  }

  private def writeInputField(name: String, tpe: Type, hasSelections: Boolean, directives: List[Directive]): Unit = {
    // TODO: This typeName stuff is weird.
    val typeName    = if (hasSelections) operationName + innerType(tpe) else innerType(tpe)
    val scalaTypeId = typeConverter.convertToScalaType(tpe, typeName, directives)
    // TODO: Default value.
    scalaWriter.writeField(name, scalaTypeId, None, "  ")
  }
}
