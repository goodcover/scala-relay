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
      birthdays {
        birthday
      }
    }
  """)
@graphql("""
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
    val foo2: relay.generated.MainQuery.Birthdays = null

    Option(foo2).map(_.birthday == null)

  }
}
