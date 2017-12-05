package example

import relay.graphql.SchemaLocation

object Impl {

  implicit val sl: SchemaLocation = SchemaLocation("Hello")
}
