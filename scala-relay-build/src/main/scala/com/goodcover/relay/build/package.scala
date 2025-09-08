package com.goodcover.relay

package object build {

  def trimBlankLines(s: String): String =
    s.replaceFirst("""^\s*(\R+|$)""", "").replaceFirst("""\R\s*$""", "")

}
