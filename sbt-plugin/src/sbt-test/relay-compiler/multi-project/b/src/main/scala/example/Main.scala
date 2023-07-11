package example

import relay.compiler.graphqlGen

object Main extends App {

  private val query = graphqlGen("""
    query MainQuery {
      task(number: 1) {
        ...TaskComponent_task
      }
    }
  """)
}
