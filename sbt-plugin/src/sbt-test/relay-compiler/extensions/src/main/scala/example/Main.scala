package example

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.Dynamic.literal
import relay.graphql.gql
import org.scalajs.dom

import scala.scalajs.js.annotation.JSImport

@gql("""
    query ExampleQuery {
      defaultSettings {
        notificationSounds
      }
      birthdays {
        birthday
      }
    }
  """)
object __query1

@gql("""
    type Birthday {
      birthday: String
    }

    extend type Query {
      birthdays: [Birthday]
    }
  """)
object frag3

case class Foo(i: Int)(val f: relay.generated.Task_foo)

trait RelayTag

object Main extends JSApp {

  def main(): Unit = {
    val m = serde.sjs.generic.deriveBindable[Foo, relay.generated.Task_foo]
    val m2 = serde.sjs.generic.deriveHoc[Foo, relay.generated.Task_foo, RelayTag]

    val q = relay.generated.Task_foo.query
    dom.console.log(q)
  }
}
