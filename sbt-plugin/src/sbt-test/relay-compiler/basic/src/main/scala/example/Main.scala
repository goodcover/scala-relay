package example

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.Dynamic.literal
import relay.graphql
import org.scalajs.dom

import scala.scalajs.js.annotation.JSImport

@graphql("""
    query MainQuery {
      defaultSettings {
        notificationSounds
      }
    }
  """)
@graphql("""
    mutation MainMutation($input: ActorSubscribeInput!) {
      actorSubscribe(input: $input) {
        clientMutationId
      }
    }
  """)
@graphql("""
    fragment Main_foo on Task {
      title
    }
  """)
object frag

case class Foo(i: Int)(val f: relay.generated.Main_foo)

object Main extends JSApp {

  def main(): Unit = {
    val q = relay.generated.MainQuery.query
    dom.console.log(q)
  }
}
