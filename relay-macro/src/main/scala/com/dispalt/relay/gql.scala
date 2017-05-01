package com.dispalt.relay

import scala.language.experimental.macros
import scala.meta.Mod.Annot
import scala.meta._
import scala.collection.immutable.Seq

class gql(arg: String) extends scala.annotation.StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    defn match {
      case cls @ Defn.Object(_, name, _) =>
        val relayOut = sys.props.get("relay.out").orNull
        val relaySchema = sys.props.get("relay.schema").orNull

        //
        val sjs = q"_root_.scala.scalajs.js"
        val str = s"$relayOut/${name.toString}.js"

        val newObj = cls.copy(mods = Seq(
          Annot(Ctor.Name(s"$sjs.native")),
          mod"""@_root_.scala.scalajs.js.annotation.JSImport($str, $sjs.annotation.JSImport.Default)"""
        ))
//        println(newObj)
        newObj
      case _ =>
        println(defn.structure)
        abort("@gql must annotate an object")
    }
  }
}
