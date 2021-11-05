package web.admin.cps

import relay.Hooks
import relay.compiler.graphqlGen
import slinky.core.{FunctionalComponent, FunctionalComponentName, KeyAddingStage}
import slinky.web.html._

object OpListFn {

  //
  val query = graphqlGen("""
query HelloQuery {
  dictionary {
    id
    definition {
      ...Friend_word
      url
    }
  }
}
""")

  //
  private val cmp = FunctionalComponent[Unit] { _ =>
    val data = Hooks.useLazyLoadQuery(query, query.newInput())

    div(s"Hello, ${data.viewer.rawId}")
  }

  def apply(): KeyAddingStage = cmp(())
}
