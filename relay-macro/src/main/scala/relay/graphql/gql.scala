package relay.graphql

import java.io.File

import sangria.ast.ObjectTypeDefinition
import sangria.parser.QueryParser
import sangria.schema.Schema
import sangria.validation.RuleBasedQueryValidator
import sangria.validation.rules._

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.util.{Failure, Success}

@compileTimeOnly("enable macro paradise to expand @gql macro annotations")
@deprecated("""Don't use this, it just slows things down, the relay compiler uses a regex to pull snippets out of scala
code, so all you really need is some annotation that looks like this one, imported as a single term.
  """,
            "0.10.3")
class gql(arg: String) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro ToGql.impl
}

private object ToGql {

  val relaySchemaPrefix        = "relaySchema="
  val relayValidateQueryPrefix = "relayValidateQuery=true"
  lazy val thinValidationRules =
    new RuleBasedQueryValidator(
      List(new ValuesOfCorrectType,
           new VariablesDefaultValueAllowed,
           new ExecutableDefinitions,
           new FieldsOnCorrectType,
           new FragmentsOnCompositeType,
           new KnownArgumentNames,
//           new KnownDirectives,
//           new KnownFragmentNames,
           new KnownTypeNames,
           new LoneAnonymousOperation,
           new NoFragmentCycles,
           new NoUndefinedVariables,
//           new NoUnusedFragments,
//           new NoUnusedVariables,
           new OverlappingFieldsCanBeMerged,
           new PossibleFragmentSpreads,
           new ProvidedNonNullArguments,
           new ScalarLeafs,
           new UniqueArgumentNames,
           new UniqueDirectivesPerLocation,
           new UniqueFragmentNames,
           new UniqueInputFieldNames,
           new UniqueOperationNames,
           new UniqueVariableNames,
           new VariablesAreInputTypes,
           new VariablesInAllowedPosition,
           new InputDocumentNonConflictingVariableInference,
           new SingleFieldSubscriptions))

  def impl(c: blackbox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val (schemaFilePath, validateQuery) = {
      val schema = c.settings.find(_.startsWith(relaySchemaPrefix)).map(_.substring(relaySchemaPrefix.length))
      val validate =
        c.settings.find(_.startsWith(relayValidateQueryPrefix)).map(_.substring(relayValidateQueryPrefix.length))
      (schema.getOrElse(
         c.abort(c.enclosingPosition,
                 s"Missing schema setting(s), add -Xmacro-settings=$relaySchemaPrefix<path-to-schema.graphql>.")),
       validate.nonEmpty)
    }

    val schemaFile = new File(schemaFilePath)

    if (!schemaFile.exists())
      c.abort(c.enclosingPosition, s"Schema file $schemaFilePath, does not exist, aborting.")

    val query = c.prefix.tree match {
      case q"new $_(${Literal(Constant(arg: String))})" => arg
      case _                                            => c.abort(c.enclosingPosition, "You must pass in a literal")
    }

    val (op, document) = QueryParser.parse(query) match {
      // Handle object type definitions.
      case Success(doc) if doc.definitions.exists(_.isInstanceOf[ObjectTypeDefinition]) =>
        None -> doc
      case Success(doc) if doc.operations.nonEmpty || doc.fragments.nonEmpty =>
        val result = doc.operations.headOption
          .flatMap(_._1)
          .orElse(doc.fragments.headOption.map(_._1))
          .getOrElse(
            c.abort(c.enclosingPosition, "Can't determine operation name make sure and give the mutation/query a name"))
        Some(result) -> doc
      case Failure(f) => c.abort(c.enclosingPosition, f.toString)
    }

    if (validateQuery && op.isDefined) {
      val schemaDoc = QueryParser
        .parse(scala.io.Source.fromFile(schemaFile).mkString)
        .getOrElse(sys.error(s"Invalid graphql schema at `$schemaFilePath`."))

      val violations = thinValidationRules.validateQuery(Schema.buildFromAst(schemaDoc), document)
      if (violations.nonEmpty)
        c.abort(c.enclosingPosition,
                s"\nFound the following violations\n\n${violations.map(f => " *  " + f.errorMessage).mkString("\n")}")
    }

    op match {
      case Some(value) =>
        val result = annottees.map(_.tree).toList match {
          case objectDef @ q"$mods object $className extends ..$template { $self => ..$body };" :: tail =>
            val opTerm = TermName(value)
            val opType = tq"_root_.relay.generated.$opTerm.type"
            val newBody = body ::: List(q"val query: _root_.relay.graphql.TaggedNode = _root_.relay.generated.$opTerm",
                                        q"val root: $opType = _root_.relay.generated.$opTerm")
            val newTemplate = tq"_root_.relay.graphql.GenericGraphQLTaggedNode" :: Nil
            q"""$mods object $className extends ..$newTemplate { $self => ..$newBody };"""
          case _ =>
            c.abort(c.enclosingPosition, "Needs to be on an object")
        }
        c.Expr[Any](result)
      case None =>
        val result = q"..${annottees.map(_.tree)}"
        c.Expr[Any](result)
    }

  }
}
