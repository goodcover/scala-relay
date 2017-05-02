package example

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.Dynamic.literal
import com.dispalt.relay.gql
import org.scalajs.dom.document

import scala.scalajs.js.annotation.JSImport

@gql("""
    query ExampleQuery($pageID: ID!) {
      defaultSettings {
        notificationSounds
      }
    }
  """)
object foo extends js.Object


@gql("""
    mutation ActorSubscribe($input: ActorSubscribeInput!) {
      actorSubscribe(input: $input) {
        clientMutationId
      }
    }
  """)
object ActorSubscribe extends js.Object

@gql("""
    fragment Task_foo on Task {
      title
    }
  """)
object frag extends js.Object


object Main extends JSApp {
  def main(): Unit = {

    ActorSubscribe
    foo
    frag

    val someFn = () => println("someFn")
  }
}
