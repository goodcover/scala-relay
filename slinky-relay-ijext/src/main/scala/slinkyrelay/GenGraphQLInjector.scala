package slinkyrelay

import org.jetbrains.plugins.scala.lang.macros.evaluator.{MacroContext, MacroImpl, ScalaMacroTypeable}
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunction
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.lang.psi.types.ScType

class GenGraphQLInjector extends ScalaMacroTypeable {
  override def checkMacro(macros: ScFunction, context: MacroContext): Option[ScType] = {

    val reg = """genGraphql\([\s]*\"\"\"([\s\S]*?)\"\"\"\)""".r

    reg.findFirstMatchIn(context.place.getText) match {
      case Some(value) =>
        val reg       = raw"(fragment|mutation|subscription|query)[\s+]([\S]+)".r
        val opAndName = for (m <- reg.findFirstMatchIn(value.group(1))) yield (m.group(1), m.group(2))
        opAndName match {
          case Some((_, name)) =>
            val tpe = s"_root_.relay.generated.${name}.type"
            ScalaPsiElementFactory.createTypeFromText(tpe, context.place, null)
          case None => None
        }
      case None => None
    }

  }

  override val boundMacro: Seq[MacroImpl] = Seq(MacroImpl("genGraphql", "relay.compiler"))
}
