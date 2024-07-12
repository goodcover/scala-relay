package example

import relay.compiler.graphqlGen
import relay.generated.TaskComponent_task

object TaskComponent {

  private val fragment = graphqlGen("""
    fragment TaskComponent_task on Task {
      title
    }
  """)

  def apply(ref: TaskComponent_task.Ref): Unit = {
    // useFragment(fragment, ref)
    // render...
  }
}
