package example

import relay.generated.TaskComponent_task
import relay.graphql

object TaskComponent {

  private val fragment = @graphqlGen("""
    fragment TaskComponent_task on Task {
      title
    }
  """)

  def apply(ref: TaskComponent_task.Ref): Unit = {
    // useFragment(fragment, ref)
    // render...
  }
}
