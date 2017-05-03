package com.dispalt.relay

import java.io.{ByteArrayInputStream}
import java.nio.charset.StandardCharsets

import sangria.parser.QueryParser

import scala.meta.Mod.Annot
import scala.meta._
import scala.collection.immutable.Seq

class gql(arg: String) extends scala.annotation.StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    defn match {
      case q"..$mods object $name extends $template" =>
        val relayOut = sys.props.get("relay.out").orNull

        val gqlStr = this match {
          case q"new $_(${Lit(arg: String)})" => arg
          case _ => abort("You must pass in a literal")
        }

        val doc = QueryParser.parse(gqlStr).getOrElse(abort(s"Failed to parse $gqlStr"))

        // TODO: Differ between GraphQLTaggedNodes, using the operation type
        val opName = doc.operations.headOption.flatMap(_._1)
          .orElse(doc.fragments.headOption.map(_._1))
          .getOrElse(abort("Can't determine operation name make sure and give the mutation/query a name"))

        //
        val sjs = q"_root_.scala.scalajs.js"
        val str = s"$relayOut/$opName.graphql.js"

        val newMods = Seq(
          Annot(Ctor.Name(s"$sjs.native")),
          mod"""@_root_.scala.scalajs.js.annotation.JSImport($str, $sjs.annotation.JSImport.Default)"""
        )
        q"..$newMods object $name extends _root_.com.dispalt.relay.GraphQLTaggedNode"
      case _ =>
        abort("@gql must annotate an object, with a literal.")
    }
  }
}


class graphql(arg: String) extends scala.annotation.StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    defn match {
      case q"..$mods val ..${Seq(patsnel: Pat.Var.Term)}: $tpeopt = $expr" =>
        /* We probably won't need this */
        val relaySchema = sys.props.get("relay.schema").orNull
        val nodeModules = sys.props.get("relay.node_modules").orNull

        val gqlStr = this match {
          case q"new $_(${Lit(arg: String)})" => arg
          case _ => abort("You must pass in a literal")
        }

        val doc = QueryParser.parse(gqlStr).getOrElse(abort(s"Failed to parse $gqlStr"))

        // TODO: Differ between GraphQLTaggedNodes, using the operation type
        val opName = doc.operations.headOption.flatMap(f => f._1)
          .orElse(doc.fragments.headOption.map(f => f._1))
          .getOrElse(abort("Can't determine operation name make sure and give the mutation/query a name"))

        println(s"Processing graphql snippet: $opName")

        val sjs = q"_root_.scala.scalajs.js"
        val tpe = t"_root_.com.dispalt.relay.GenericGraphQLTaggedNode"

        val out = Helper.runNode(relaySchema, nodeModules, gqlStr)

        val res = q"""val ${patsnel: Pat.Var.Term}: $tpe = new _root_.com.dispalt.relay.GenericGraphQLTaggedNode {
              val raw = $sjs.JSON.parse($out)
            }
         """
        res

      case defn =>
        println(defn.structure)
        abort("@graphql must annotate a val.\nThe annotation also needs a strict literal string in the first position.")
    }
  }
}


private[relay] object Helper {
  import sys.process._

  def runNode(relaySchema: String, nodeModulesParent: String, gql: String) = {

    val gqlStream = new ByteArrayInputStream(gql.getBytes(StandardCharsets.UTF_8))

    val run = Process(
      command  = s"""node ./node_modules/scala-relay-compiler/bin/src-stdin.js --schema $relaySchema --out $nodeModulesParent/out""",
      cwd = new java.io.File(nodeModulesParent)
      ) #< gqlStream !! ProcessLogger(
      { f => println(f) },
      { err => abort(err) }
    )

    val reg = """(?s)---GQL:SCALA---(.*)---EGQL:SCALA---""".r
    reg
      .findFirstMatchIn(run)
      .map(_.group(1))
      .getOrElse(abort(s"Failed to parse output\n\n$run"))
  }
}
