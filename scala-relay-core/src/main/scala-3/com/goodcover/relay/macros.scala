package com.goodcover.relay

import scala.quoted.*

object macros {

  private val reg = raw"(fragment|mutation|subscription|query)[\s]+([\w]+)".r

  /**
   * Buckle up.
   *
   * This is a dummy function that basically acts as a marker so that the js
   * type generation can process the GQL statements, and this will substitute,
   * the generated code in question. That generated code has some types attached
   * to it to make all the scala magic work.
   *
   * it attaches a type like GQLTaggedNode[Input <: js.Any, Output <: js.Any].
   * This gives any sort of call which needs to deal with the input or output
   * enough information to be useful.
   *
   * HOWEVER, there are some real rules involved.
   *
   * 1) It has to be a string literal, with triple quotes, """ this is because
   * regexes are used on the js side to make this work. Eventually we could use
   * scala.meta to make this a lot better. 2) it can't be a final val s = "foo"
   * either. it has to be a plan string. 3) You cannot use strip margin, strip
   * margin makes it not a literal string. 4) !IMPORTANT! THE WORD graphql in
   * all lowercase must show up in the file.
   *
   * @param s
   *   plan old graphql string.
   * @return
   *   Type of the Object generated from the relay-compiler
   */
  inline def graphqlGen(s: String): Any = ${ graphqlGenImpl('s) }

  private def graphqlGenImpl(s: Expr[String])(using Quotes): Expr[Any] = {
    import quotes.reflect.*

    s match {
      case Expr(stringValue: String) =>

        val opAndName = reg.findFirstMatchIn(stringValue).map(m => (m.group(1), m.group(2)))

        opAndName match {
          case Some((_, name)) =>
            // Generate the reference to the generated object
            val termRef = Ref(Symbol.requiredModule(s"relay.generated.$name"))
            termRef.asExprOf[Any]
          case None            =>
            report.errorAndAbort(s"$stringValue doesn't seem valid. Regex $reg doesn't match.")
        }
      case _ =>
        report.errorAndAbort("Must provide a string literal.")
    }
  }
}
