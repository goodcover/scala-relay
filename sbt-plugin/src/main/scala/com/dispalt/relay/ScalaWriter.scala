package com.dispalt.relay

import caliban.parsing.Parser
import caliban.parsing.adt.Definition.ExecutableDefinition.OperationDefinition
import caliban.parsing.adt.Type.innerType
import caliban.parsing.adt.{Directive, OperationType, Selection}
import com.dispalt.relay.GraphQLText.appendOperationText
import sbt.*
import sbt.io.Using.fileWriter

import java.io.Writer
import java.nio.charset.StandardCharsets

// TODO: Rename
class ScalaWriter(outputDir: File, schema: GraphQLSchema) {

  // It would be nice to use Scalameta for this but it doesn't support comments which kinda sucks.
  // See https://github.com/scalameta/scalameta/issues/3372.

  def write(file: File): Set[File] = {
    write(IO.read(file, StandardCharsets.UTF_8))
  }

  def write(documentText: String): Set[File] = {
    val document = Parser.parseQuery(documentText).right.get
    document.operationDefinitions.map(writeOperation(documentText, _)).toSet
  }

  private def writeOperation(documentText: String, operation: OperationDefinition): File = {
    operation.operationType match {
      case OperationType.Query        => writeQuery(documentText, operation)
      case OperationType.Mutation     => writeMutation(documentText, operation)
      case OperationType.Subscription => writeSubscription(documentText, operation)
    }
  }

  private def writeQuery(documentText: String, operation: OperationDefinition): File = {
    // From: handleQuery and out
    val file = operationFile(operation)
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      writePreamble(writer, documentText, operation)
      writer.write("\n")
      writeInputType(writer, operation)
      writer.write("\n")
      writeQueryTrait(writer, operation)
      writer.write("\n")
      writeQueryObject(writer, operation)
    }
    file
  }

  private def writeMutation(documentText: String, operation: OperationDefinition): File = {
    // From: handleQuery and out
    val file = operationFile(operation)
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      writePreamble(writer, documentText, operation)
      writeInputType(writer, operation)
    // TODO
    //writeMutationCompanion(writer, operation)
    }
    file
  }

  private def writeSubscription(documentText: String, operation: OperationDefinition): File = {
    // From: handleQuery and out
    val file = operationFile(operation)
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      writePreamble(writer, documentText, operation)
      ???
    }
    file
  }

  private def writePreamble(writer: Writer, documentText: String, operation: OperationDefinition): Unit = {
    writer.write(s"""package relay.generated
         |
         |import _root_.scala.scalajs.js
         |import _root_.scala.scalajs.js.|
         |
         |""".stripMargin)
    writer.write("/*\n")
    appendOperationText(documentText, operation) { line =>
      writer.write(line.replace("*/", "*\\/"))
      val last = line.lastOption
      if (!last.contains('\n') && !last.contains('\f')) {
        writer.write("\n")
      }
    }
    writer.write("*/\n")
  }

  // TODO: Don't do this. We should create shared types from the schema.
  private def writeInputType(writer: Writer, operation: OperationDefinition): Unit = {
    writer.write("trait ")
    // TODO: When does an operation not have a name?
    val operationName = operation.name.get
    writer.write(operationName)
    writer.write("Input extends js.Object {\n")
    operation.variableDefinitions.foreach { variable =>
      // TODO: Handle directives.
      // TODO: Handle defaults.
      writer.write("  val ")
      writer.write(variable.name)
      writer.write(": ")
      // TODO: Type
      //  1) transformInputType
      //  2) makeTypeFromMember
      // TODO: transformInputType used to ask the schema if this was nonNull.
      //      if (variable.variableType.nonNull) {
      //        variable.variableType match {
      //          case Type.NamedType(name, nonNull) => ???
      //          case Type.ListType(ofType, nonNull) => ???
      //        }
      //      } else {
      //
      //      }
      writer.write(innerType(variable.variableType))
    }
    writer.write("}\n")
  }

  private def writeQueryTrait(writer: Writer, operation: OperationDefinition): Unit = {
    writer.write("trait ")
    // TODO: When does an operation not have a name?
    val operationName = operation.name.get
    writer.write(operationName)
    writer.write(" extends js.Object {\n")
    operation.selectionSet.foreach {
      case field: Selection.Field             => writeQueryField(writer, field, operationName)
      case spread: Selection.FragmentSpread   => ???
      case fragment: Selection.InlineFragment => ???
    }
    writer.write("}\n")
  }

  private def writeQueryObject(writer: Writer, operation: OperationDefinition): Unit = {
    writer.write("object ")
    // TODO: When does an operation not have a name?
    val operationName = operation.name.get
    writer.write(operationName)
    writer.write(" _root_.relay.gql.QueryTaggedNode[")
    writer.write(operationName)
    writer.write("Input, ")
    writer.write(operationName)
    writer.write("""] {
                   |  def newInput(""".stripMargin)
    // TODO: Parameters.
    writer.write(") = ???\n")
    writer.write("}\n")
  }

  private def writeQueryField(writer: Writer, field: Selection.Field, companionName: String): Unit = {
    // From: closeField
    // TODO: Handle directives
    // TODO: Handle alias
    // We ignore arguments since we only use this as an output type.
    writer.write("  def ")
    writer.write(field.name)
    writer.write(": ")
    writer.write(companionName)
    writer.write(".")
    writer.write(field.name.capitalize)
    if (!schema.queryField(field.name).ofType.nonNull) {
      writer.write(" | Null")
    }
    writer.write("\n")
  }

  /**
    * @param writer The writer to write the type to.
    * @param documentType The type of artifact that this module represents.
    * @param docText The actual document that this module represents.
    * @param concreteText The IR for the document that this module represents.
    * @param directives The directives from the definition.
    * @param typeText The type information generated for the GraphQL selections made.
    * @param hash A hash of the concrete node including the query text.
    * @param sourceHash A hash of the document, which is used by relay-compiler to know if it needs to write a new version of the artifact.
    */
  private def writeThing(
    writer: Writer,
    documentType: String,
    docText: String,
    concreteText: String,
    directives: List[Directive],
    typeText: String,
    hash: String,
    sourceHash: String
  ): Unit = {
    // TODO: Test this.
    val maybeRefetchableQueryName = directives.find(_.name == "refetchable").map(_.arguments("queryName").toInputString)

    val queryTypeParams =
      if (documentType == "ReaderFragment") "[Ctor, Out]"
      else if (documentType == "ReaderInlineDataFragment") "[Ctor, Out]"
      else ""

    // TODO: Minify.
    val code = concreteText;

    writer.write("/*\n")
    writer.write(docText.replace("*/", "*\\/"))
    writer.write("\n*/\n")
    writer.write(typeText)
    writer.write("""
                 |  // Used to differentiate between normal and inline query types.
                 |  type Query = _root_.relay.gql.""".stripMargin)
    writer.write(documentType)
    writer.write(queryTypeParams)
    writer.write("""
                 |
                 |  lazy val query: Query = {
                 |    val defn = _root_.scala.scalajs.js.Function(""".stripMargin)
    writer.write("\"\"\"return ")
    writer.write(code)
    writer.write("\"\"\"")
    writer.write(""").call(null)
                 |    """.stripMargin)
    maybeRefetchableQueryName.foreach { queryName =>
      writer.write("""// Refetchable query
          |    defn.metadata.refetch.operation = _root_.relay.generated.""".stripMargin)
      writer.write(queryName)
      writer.write(".query")
    }
    writer.write("""
                 |    defn.asInstanceOf[Query]
                 |  }
                 |  lazy val sourceHash: String = """".stripMargin)
    writer.write(sourceHash)
    writer.write(""""
                 |
                 |}
                 |""".stripMargin)
  }

  private def operationFile(operation: OperationDefinition) =
    // TODO: When does an operation not have a name?
    outputDir / s"${operation.name.get}.scala"
}

object ScalaWriter {

  case class InvalidFieldSelection(fieldName: String) extends Exception(s"Invalid field selection: $fieldName")
}
