package relay.graphql

import java.io.File

import sangria.parser.QueryParser

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scala.util.{Failure, Success}

class gql(arg: String) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro ToGql.impl

//  inline def apply(defn: Any): Any = meta {
//    defn match {
//      case q"..$mods object $name extends $template" =>
//        val gqlStr = this match {
//          case q"new $_(${Lit(arg: String)})" => arg
//          case _ => abort("You must pass in a literal")
//        }
//
//        val op = QueryParser.parse(gqlStr) match {
//          case Success(doc) => doc.operations.headOption.flatMap(_._1)
//            .orElse(doc.fragments.headOption.map(_._1))
//            .getOrElse(abort("Can't determine operation name make sure and give the mutation/query a name"))
//
//          case Failure(f) => abort(f.toString)
//        }
//
//        val newTemplate = ctor"_root_.relay.graphql.GenericGraphQLTaggedNode"
//        val opTerm = Term.Name(op)
//
//        q"""..$mods object $name extends $newTemplate {
//           val query: _root_.relay.graphql.TaggedNode = _root_.relay.generated.$opTerm
//         }
//         """
//      case _ =>
//        abort("@gql must annotate an object, with a literal.")
//    }
//  }
}

object ToGql {

  val relaySchemaPrefix = "relaySchema="

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val schemaFilePath = {
      val schema = c.settings.find(_.startsWith(relaySchemaPrefix)).map(_.substring(relaySchemaPrefix.length))
      schema.getOrElse(
        c.abort(c.enclosingPosition,
                s"Missing schema setting(s), add -Xmacro-settings=$relaySchemaPrefix<path-to-schema.graphql>."))
    }

    if (!new File(schemaFilePath).exists())
      c.abort(c.enclosingPosition, s"Schema file $schemaFilePath, does not exist, aborting.")

    val query = c.prefix.tree match {
      case q"new $_(${Literal(Constant(arg: String))})" => arg
      case _                                            => c.abort(c.enclosingPosition, "You must pass in a literal")
    }

    val op = QueryParser.parse(query) match {
      case Success(doc) =>
        doc.operations.headOption
          .flatMap(_._1)
          .orElse(doc.fragments.headOption.map(_._1))
          .getOrElse(
            c.abort(c.enclosingPosition, "Can't determine operation name make sure and give the mutation/query a name"))

      case Failure(f) => c.abort(c.enclosingPosition, f.toString)
    }

    val result = annottees.map(_.tree).toList match {
      case objectDef @ q"$mods object $className extends ..$template { $self => ..$body };" :: tail =>
        val newBody = body ::: List(
          q"val query: _root_.relay.graphql.TaggedNode = _root_.relay.generated.${TermName(op)}")
        val newTemplate = tq"_root_.relay.graphql.GenericGraphQLTaggedNode" :: Nil
        q"""$mods object $className extends ..$newTemplate { $self => ..$newBody };"""
      case _ =>
        c.abort(c.enclosingPosition, "Needs to be on an object")
    }
    c.Expr[Any](result)
  }
}
