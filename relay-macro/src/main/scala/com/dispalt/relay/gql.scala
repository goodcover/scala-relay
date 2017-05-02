package com.dispalt.relay

import sangria.parser.QueryParser

import scala.language.experimental.macros
import scala.meta.Mod.Annot
import scala.meta._
import scala.collection.immutable.Seq

class gql(arg: String) extends scala.annotation.StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    defn match {
      case cls @ Defn.Object(_, name, _) =>
        val relayOut = sys.props.get("relay.out").orNull
        /* We probably won't need this */
        val relaySchema = sys.props.get("relay.schema").orNull

        val gqlStr = this match {
          case q"new $_(${Lit(arg: String)})" => arg
          case _ => abort("You must pass in a literal")
        }

        val doc = QueryParser.parse(gqlStr).getOrElse(abort(s"Failed to parse $gqlStr"))
        val opName = doc.operations.headOption.flatMap(_._1).getOrElse("Can't determine operation name" +
          "make sure and give the mutation/query a name")

        //
        val sjs = q"_root_.scala.scalajs.js"
        val str = s"$relayOut/$opName.graphql.js"

        val newObj = cls.copy(mods = Seq(
          Annot(Ctor.Name(s"$sjs.native")),
          mod"""@_root_.scala.scalajs.js.annotation.JSImport($str, $sjs.annotation.JSImport.Default)"""
        ))
        newObj
      case _ =>
        abort("@gql must annotate an object, with a literal.")
    }
  }
}
