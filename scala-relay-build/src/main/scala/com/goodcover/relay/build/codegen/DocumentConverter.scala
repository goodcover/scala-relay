package com.goodcover.relay.build.codegen

import caliban.parsing.Parser
import caliban.parsing.adt.Definition.ExecutableDefinition.{FragmentDefinition, OperationDefinition}
import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.InputObjectTypeDefinition
import caliban.parsing.adt.{Document, OperationType}
import com.goodcover.relay.build.{FileOps, GraphQLSchema}

import java.io.{File, FileWriter}
import java.nio.charset.StandardCharsets

class DocumentConverter(outputDir: File, schema: GraphQLSchema, typeMappings: Map[String, String], outputs: Set[File]) {
  import FileOps._

  private val typeConverter = new TypeConverter(schema, typeMappings)

  def convertSchema(): Set[File] = {
    val inputs = schema.inputObjectTypes.values.map(writeInput(_, schema.document)).toSet
    inputs
  }

  def convert(file: File): Set[File] = {
    val source = scala.io.Source.fromFile(file, StandardCharsets.UTF_8.name())
    try {
      val documentText = source.mkString
      convert(documentText)
    } finally {
      source.close()
    }
  }

  def convert(documentText: String): Set[File] = {
    val document = Parser.parseQuery(documentText).right.get
    // TODO: GC-3158 - Remove documentText from these.
    val operations = document.operationDefinitions.map(writeOperation(_, documentText, document)).toSet
    val fragments  = document.fragmentDefinitions.map(writeFragment(_, documentText)).toSet
    val refetchables = {
      for {
        fragment    <- document.fragmentDefinitions
        refetchable <- getRefetchable(fragment.directives)
      } yield writeRefetchableFragment(fragment, refetchable, documentText, document)
    }.toSet
    operations ++ fragments ++ refetchables
  }

  private def writeInput(input: InputObjectTypeDefinition, document: Document): File = {
    val file = inputFile(input)
    outputDir.mkdirs()
    val writer = new FileWriter(file, StandardCharsets.UTF_8)
    try {
      val definitionWriter = new InputWriter(writer, input, document, schema, typeConverter)
      definitionWriter.write()
      file
    } finally {
      writer.close()
    }
  }

  private def writeOperation(operation: OperationDefinition, documentText: String, document: Document): File = {
    val file = operationFile(operation)
    outputDir.mkdirs()
    val writer = new FileWriter(file, StandardCharsets.UTF_8)
    try {
      val definitionWriter = operation.operationType match {
        case OperationType.Query =>
          new QueryWriter(writer, operation, documentText, document, schema, typeConverter)
        case OperationType.Mutation =>
          new MutationWriter(writer, operation, documentText, document, schema, typeConverter)
        case OperationType.Subscription =>
          new SubscriptionWriter(writer, operation, documentText, document, schema, typeConverter)
      }
      definitionWriter.write()
      file
    } finally {
      writer.close()
    }
  }

  private def writeFragment(fragment: FragmentDefinition, documentText: String): File = {
    val file = fragmentFile(fragment)
    outputDir.mkdirs()
    val writer = new FileWriter(file, StandardCharsets.UTF_8)
    try {
      val definitionWriter = new FragmentWriter(writer, fragment, documentText, schema, typeConverter)
      definitionWriter.write()
      file
    } finally {
      writer.close()
    }
  }

  private def writeRefetchableFragment(
    fragment: FragmentDefinition,
    refetchable: Refetchable,
    documentText: String,
    document: Document
  ): File = {
    val file = refetchableFile(fragment, refetchable)
    outputDir.mkdirs()
    val writer = new FileWriter(file, StandardCharsets.UTF_8)
    try {
      val definitionWriter =
        new RefetchableFragmentWriter(writer, fragment, refetchable, documentText, document, schema, typeConverter)
      definitionWriter.write()
      file
    } finally {
      writer.close()
    }
  }

  private def inputFile(input: InputObjectTypeDefinition): File =
    new File(outputDir, s"${input.name}.scala")

  private def operationFile(operation: OperationDefinition): File =
    new File(outputDir, s"${getOperationName(operation)}.scala")

  private def fragmentFile(fragment: FragmentDefinition): File =
    new File(outputDir, s"${fragment.name}.scala")

  private def refetchableFile(fragment: FragmentDefinition, refetchable: Refetchable): File =
    new File(outputDir, s"${refetchable.queryName}.scala")

  // Placeholder implementations - these would need to be copied from the original codegen package
  private def getRefetchable(directives: List[caliban.parsing.adt.Directive]): Option[Refetchable] = None

  private def getOperationName(operation: OperationDefinition): String =
    operation.name.getOrElse("UnnamedOperation")
}

object DocumentConverter {
  def getOperationName(operation: OperationDefinition): String =
    operation.name.getOrElse("UnnamedOperation")

  def variableArguments(operation: OperationDefinition): String = ""
}

// The actual writer implementations are now in separate files
