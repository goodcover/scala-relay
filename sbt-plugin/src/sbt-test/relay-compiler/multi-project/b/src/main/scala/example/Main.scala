package example

import relay.compiler.graphqlGen
import relay.generated._

object Main extends App {

  private val query: MainQuery.type = graphqlGen("""
    query MainQuery {
      task(number: 1) {
        ...TaskComponent_task
      }
    }
  """)

  private val refetchable: Main_fragment.type = graphqlGen("""
    fragment Main_fragment on Query @refetchable(queryName: "Main_fragment_Query") {
      task(number: $num) {
        ...TaskComponent_task
      }
    }
  """)

  // Make sure that everything can be linked properly.
  private val queryObj = MainQuery
  private val fragmentObj = Main_fragment
  private val refetchableObj = Main_fragment_Query
  private val spreadObj = TaskComponent_task
}
