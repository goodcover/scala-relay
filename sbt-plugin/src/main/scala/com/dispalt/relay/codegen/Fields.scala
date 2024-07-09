package com.dispalt.relay.codegen

object Fields {

  def isTypeName(name: String): Boolean =
    name == "__typename"

  def isMetaField(name: String): Boolean =
    name.startsWith("__")
}
