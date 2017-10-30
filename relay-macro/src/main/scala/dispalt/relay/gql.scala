package dispalt.relay

import sangria.parser.QueryParser

import scala.meta._
import scala.util.{Failure, Success}

class gql(arg: String) extends scala.annotation.StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    defn match {
      case q"..$mods object $name extends $template" =>
        val gqlStr = this match {
          case q"new $_(${Lit(arg: String)})" => arg
          case _ => abort("You must pass in a literal")
        }

        QueryParser.parse(gqlStr) match {
          case Success(f) => ()
          case Failure(f) => abort(f.toString)
        }

        q"..$mods object $name extends $template"
      case _ =>
        abort("@gql must annotate an object, with a literal.")
    }
  }
}
