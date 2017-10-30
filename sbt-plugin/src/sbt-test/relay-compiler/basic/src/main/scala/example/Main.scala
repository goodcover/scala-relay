package example

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.Dynamic.literal
import dispalt.relay.gql
import org.scalajs.dom

import scala.scalajs.js.annotation.JSImport

@gql("""
    query ExampleQuery {
      defaultSettings {
        notificationSounds
      }
    }
  """)
object foo

@gql("""
    mutation ActorSubscribe($input: ActorSubscribeInput!) {
      actorSubscribe(input: $input) {
        clientMutationId
      }
    }
  """)
object ActorSubscribe

@gql("""
    fragment Task_foo on Task {
      title
    }
  """)
object frag

object Main extends JSApp {

  def main(): Unit = {

    val q = relay.generated.Task_foo.query
    dom.console.log(q)
  }
}
