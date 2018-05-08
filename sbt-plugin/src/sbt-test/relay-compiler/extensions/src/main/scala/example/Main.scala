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

object Main extends JSApp {
  def main(): Unit = {
    val foo2: relay.generated.ExampleQuery.Birthdays = null

    Option(foo2).map(_.birthday == null)

  }
}
