package com.goodcover.relay.build.codegen

object Fields {

  def isTypeName(name: String): Boolean =
    name == "__typename"

  def isID(name: String): Boolean =
    name == "__id"

  def isMetaField(name: String): Boolean =
    name.startsWith("__")
}
