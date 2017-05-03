package com.dispalt.relay

import org.scalatest._
import sys.process._

class ProcessSpec extends FlatSpec {

  behavior of "process spec"

  it should "gql" in {
    // This is dumb but whatever.
    val cwd = new java.io.File("relay-macro/src/test/resources/helper")
    val schema = new java.io.File("relay-macro/src/test/resources/helper/schema.graphql")
    println(
      Process(
        "yarn install",
        cwd
      ).!!
    )

    val gql = """
      query Root_foo {
        dictionary {
          id
        }
      }
    """

    //
    println(Helper.runNode(schema.getAbsolutePath, cwd.getAbsolutePath, gql))
  }
}
