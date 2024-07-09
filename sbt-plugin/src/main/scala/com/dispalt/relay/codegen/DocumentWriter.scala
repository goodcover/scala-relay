package com.dispalt.relay.codegen

import caliban.parsing.Parser
import caliban.parsing.adt.Definition.ExecutableDefinition.{FragmentDefinition, OperationDefinition}
import caliban.parsing.adt.OperationType
import com.dispalt.relay.GraphQLSchema
import com.dispalt.relay.codegen.DocumentWriter.getOperationName
import sbt._
import sbt.io.Using.fileWriter

import java.io.Writer
import java.nio.charset.StandardCharsets

class DocumentWriter(outputDir: File, schema: GraphQLSchema, typeMappings: Map[String, String], outputs: Set[File]) {

  def write(file: File): Set[File] = {
    write(IO.read(file, StandardCharsets.UTF_8))
  }

  def write(documentText: String): Set[File] = {
    val document   = Parser.parseQuery(documentText).right.get
    val operations = document.operationDefinitions.map(writeOperation(documentText, _)).toSet
    val fragments  = document.fragmentDefinitions.map(writeFragment(documentText, _)).toSet
    operations ++ fragments
  }

  private def writeOperation(documentText: String, operation: OperationDefinition): File = {
    operation.operationType match {
      case OperationType.Query        => writeQuery(documentText, operation)
      case OperationType.Mutation     => writeMutation(documentText, operation)
      case OperationType.Subscription => writeSubscription(documentText, operation)
    }
  }

  private def writeQuery(documentText: String, query: OperationDefinition): File =
    writeDefinition(operationFile(query))(new QueryWriter(_, query, documentText, schema, typeMappings))

  private def writeMutation(documentText: String, mutation: OperationDefinition): File =
    writeDefinition(operationFile(mutation))(new MutationWriter(_, mutation, documentText, schema, typeMappings))

  private def writeSubscription(documentText: String, subscription: OperationDefinition): File =
    writeDefinition(operationFile(subscription))(new SubscriptionWriter(_, subscription, documentText, schema, typeMappings))

  private def writeFragment(documentText: String, fragment: FragmentDefinition): File =
    writeDefinition(fragmentFile(fragment))(new FragmentWriter(_, fragment, documentText, schema, typeMappings))

  private def writeDefinition(file: File)(f: Writer => ExecutableDefinitionWriter): File =
    fileWriter(StandardCharsets.UTF_8)(file) { writer =>
      f(writer).write()
      file
    }

  private def operationFile(operation: OperationDefinition) =
    outputFile(getOperationName(operation))

  private def fragmentFile(fragment: FragmentDefinition) =
    outputFile(fragment.name)

  private def outputFile(name: String) = {
    val file = outputDir.getAbsoluteFile / s"$name.scala"
    if (file.exists()) {
      if (outputs.contains(file))
        throw new IllegalArgumentException(
          s"File $file already exists. Ensure that you only have one fragment or operation named $name."
        )
      else file.delete()
    }
    file
  }
}

object DocumentWriter {

  // TODO: Where should this go?
  private[codegen] def getOperationName(operation: OperationDefinition) =
    operation.name.getOrElse(throw new UnsupportedOperationException("Anonymous operations are not not supported."))
}
