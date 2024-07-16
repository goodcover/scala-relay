package com.goodcover.relay

import scala.reflect.macros.whitebox

object macros {

  /**
    * Buckle up.
    *
    * This is a dummy function that basically acts as a marker so that the js type generation can process
    * the GQL statements, and this will substitute, the generated code in question.  That generated code has
    * some types attached to it to make all the scala magic work.
    *
    * it attaches a type like GQLTaggedNode[Input <: js.Any, Output <: js.Any].  This gives any sort of call
    * which needs to deal with the input or output enough information to be useful.
    *
    * HOWEVER, there are some real rules involved.
    *
    *  1) It has to be a string literal, with triple quotes, """
    *     this is because regexes are used on the js side to make this work.  Eventually we could use scala.meta
    *     to make this a lot better.
    *  2) it can't be a final val s = "foo" either. it has to be a plan string.
    *  3) You cannot use strip margin, strip margin makes it not a literal string.
    *  4) !IMPORTANT! THE WORD graphql in all lowercase must show up in the file.
    *
    * @param s plan old graphql string.
    * @return Type of the Object generated from the relay-compiler
    */
  def graphqlGen(s: String): Any = macro genGraphqlMacrosImpl.impl

  object genGraphqlMacrosImpl {
    def impl(c: whitebox.Context)(s: c.Expr[String]): c.Expr[Any] = {

      import c.universe._

      s.tree match {
        case Literal(Constant(s: String)) =>
          val reg       = raw"(fragment|mutation|subscription|query)[\s]+([\w]+)".r
          val opAndName = for (m <- reg.findFirstMatchIn(s)) yield (m.group(1), m.group(2))

          opAndName match {
            case Some((_, name)) => c.Expr(q"_root_.relay.generated.${TermName(name)}")
            case None            => c.abort(c.enclosingPosition, s"${s} doesn't seem valid.  Regex $reg doesn't match.")
          }
        case _ => c.abort(c.enclosingPosition, "Must provide a string literal.")
      }
    }
  }
}
