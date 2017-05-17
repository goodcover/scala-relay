package dispalt.relay


import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/**
  * The idea was to use an old macro to spit out a string literal (the settings) and
  * then use another new inline macro to use that to then generate
  */
object GraphQLSettings {

  private final val relaySchemaPrefix = "relay.schema="
  private final val relayOutPrefix = "relay.out="

  def apply(prefix: String, name: String): String = macro macroImpl

  def macroImpl(c: blackbox.Context)(prefix: c.Expr[String], name: c.Expr[String]): c.Tree = {
    import c.universe._

    val q"${Literal(Constant(pref: String))}" = prefix.tree

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

    val out = getSetting(pref)

    Literal(Constant(out))
  }
}
