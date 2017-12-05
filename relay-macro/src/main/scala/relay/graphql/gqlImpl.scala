package relay.graphql

import sangria.parser.QueryParser

import scala.meta._
import scala.meta.Defn.{Class, Object, Trait}
import scala.util.{Failure, Success}

object gqlImpl {

  def gql(gqlStr: String, defn: Any): Stat = {
    defn match {
      case Object(mods, name, template) =>
        val op = QueryParser.parse(gqlStr) match {
          case Success(doc) =>
            doc.operations.headOption
              .flatMap(_._1)
              .orElse(doc.fragments.headOption.map(_._1))
              .getOrElse(abort("Can't determine operation name make sure and give the mutation/query a name"))

          case Failure(f) => abort(f.toString)
        }

        val newTemplate = ctor"_root_.relay.graphql.GenericGraphQLTaggedNode"
        val opTerm      = Term.Name(op)

        q"""..$mods object $name extends $newTemplate {
             val query: _root_.relay.graphql.TaggedNode = _root_.relay.generated.$opTerm
           }
           """
      case _ =>
        abort("@gql must annotate an object, with a literal.")
    }
  }

}
