package example

import relay.compiler.graphqlGen
import relay.generated._
import org.scalajs.dom

object Main extends App {

  private val query: MainQuery.type = graphqlGen("""
    query MainQuery {
      task(number: 1) {
        ...TaskComponent_task
      }
      story {
        ...StoryComponent_story
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
  dom.console.log(MainQuery.sourceHash)
  dom.console.log(Main_fragment.sourceHash)
  dom.console.log(Main_fragment_Query.sourceHash)
  dom.console.log(TaskComponent_task.sourceHash)
  dom.console.log(StoryComponent_story.sourceHash)
}
