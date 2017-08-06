package dispalt.relay


import scala.language.experimental.macros
import scala.reflect.macros.blackbox

case class GraphQLSettings(schema: String, out: String)

/**
  * The idea was to use an old macro to spit out a string literal (the settings) and
  * then use another new inline macro to use that to then generate
  */
object GraphQLSettings {

  final val relaySchemaPrefix = "relay.schema="
  final val relayOutPrefix = "relay.out="


  implicit def materialize: GraphQLSettings = macro macroImpl

  def macroImpl(c: blackbox.Context): c.Tree = {
    import c.universe._

//    val q"${Literal(Constant(pref: String))}" = prefix.tree

    def getSetting(prefix: String): String = {
      c.settings
        .find(_.startsWith(prefix))
        .map(s => s.substring(prefix.length))
        .getOrElse(
          c.abort(
            c.enclosingPosition,
            s"Missing setting(s), add -Xmacro-settings=$prefix<foo> to scalacOptions.")
        )
    }

    val out = getSetting(relayOutPrefix)
    val schema = getSetting(relaySchemaPrefix)

    q"""
       dispalt.relay.GraphQLSettings($schema, $out)
     """
  }
}
