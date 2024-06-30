package com.dispalt.relay

import caliban.parsing.Parser
import caliban.parsing.adt.Document
import sbt.*

class GraphQLSchema {

}

object GraphQLSchema {

  def apply(file: File): GraphQLSchema = {
    val document = Parser.parseQuery(IO.read(file)).right.get
    GraphQLSchema(document)
  }

  def apply(document: Document): GraphQLSchema = {
    new GraphQLSchema
  }
}
