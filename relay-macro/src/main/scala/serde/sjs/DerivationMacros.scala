package serde.sjs

import scala.reflect.macros.blackbox
import scala.scalajs.js

final class DerivationMacros(val c: blackbox.Context) {

  import c.universe._

  private[this] val jsAnyType = typeOf[js.Any]

  private[this] case class Member(name: TermName, decodedName: String, tpe: Type)
  private[this] case class RawMember(name: TermName, decodedName: String)

  private[this] def membersFromParamList(paramList: List[Symbol], tpe: Type): List[Member] = {
    paramList.map { param =>
      Member(param.name.toTermName,
             param.name.decodedName.toString,
             tpe.decl(param.name).typeSignature.resultType.asSeenFrom(tpe, tpe.typeSymbol))
    }
  }

  private[this] def membersFromPrimaryCtor(tpe: Type,
                                           rawRequired: Boolean): Option[(List[Member], Option[RawMember])] = {
    tpe.decls.collectFirst {
      case primaryCtor: MethodSymbol if primaryCtor.isPrimaryConstructor =>
        val paramLists = primaryCtor.paramLists
        val members    = membersFromParamList(paramLists.head, tpe)

        val rawMember = if (rawRequired) {
          if (paramLists.size <= 1) {
            c.abort(c.enclosingPosition, "the second parameter list doesn't exist")
          }

          if (paramLists.tail.head.size != 1) {
            c.abort(c.enclosingPosition, "the second parameter list must have exactly one parameter")
          }

          val member = membersFromParamList(paramLists.tail.head, tpe).head

          if (!(member.tpe =:= jsAnyType)) {
            c.abort(c.enclosingPosition,
                    "the only parameter of the second parameter list must be an scala.scalajs.js.Any")
          }

          Some(RawMember(member.name, member.decodedName))
        } else {
          None
        }

        (members, rawMember)
    }
  }

  private[this] val encoderTypeCtor: Type = typeOf[Encoder[_]].typeConstructor
  private[this] val decoderTypeCtor: Type = typeOf[Decoder[_]].typeConstructor

  private[this] case class Instance(typeCtor: Type, tpe: Type, name: TermName) {

    def resolve(): Tree = c.inferImplicitValue(appliedType(typeCtor, List(tpe))) match {
      case EmptyTree      => c.abort(c.enclosingPosition, s"could not find implicit $typeCtor[$tpe]")
      case instance: Tree => instance
    }
  }

  private[this] case class Instances(encoder: Instance, decoder: Instance)

  private[this] case class ProductRepr(members: List[Member]) {

    lazy val instances: List[Instances] = {
      members.reverse.foldLeft(List.empty[Instances]) {
        case (accumulated, Member(_, _, tpe)) if !accumulated.exists(_.encoder.tpe =:= tpe) =>
          val instances = Instances(Instance(encoderTypeCtor, tpe, TermName(c.freshName("encoder"))),
                                    Instance(decoderTypeCtor, tpe, TermName(c.freshName("decoder"))))

          instances :: accumulated
      }
    }

    private[this] def fail(tpe: Type): Nothing = {
      c.abort(c.enclosingPosition, s"invalid member type $tpe")
    }

    def encoder(tpe: Type): Instance = {
      instances.map(_.encoder).find(_.tpe =:= tpe).getOrElse(fail(tpe))
    }

    def decoder(tpe: Type): Instance = {
      instances.map(_.decoder).find(_.tpe =:= tpe).getOrElse(fail(tpe))
    }
  }

  def encoder[A: c.WeakTypeTag]: c.Expr[Encoder[A]] = {
    val tpe = weakTypeOf[A]

    membersFromPrimaryCtor(tpe, rawRequired = false)
      .fold(c.abort(c.enclosingPosition, s"could not find the primary constructor of $tpe")) {
        case (members, _) =>
          val repr = ProductRepr(members)

          val instanceDefs = repr.instances.map(_.encoder).map {
            case instance @ Instance(_, instanceType, instanceName) =>
              q"""
              val $instanceName: _root_.serde.sjs.Encoder[$instanceType] =
                ${instance.resolve()}
            """
          }

          val params = repr.members.map {
            case Member(memberName, memberDecodedName, memberType) =>
              q"""
              _root_.scala.Tuple2.apply[_root_.java.lang.String, _root_.scala.scalajs.js.Any](
                ${Literal(Constant(memberDecodedName))},
                ${repr.encoder(memberType).name}(a.$memberName)
              )
            """
          }

          c.Expr[Encoder[A]](q"""
            _root_.serde.sjs.Encoder.instance { a =>
              ..$instanceDefs
              _root_.scala.scalajs.js.Dynamic.literal(..$params)
            }
          """)
      }
  }

  def decoder[A: c.WeakTypeTag]: c.Expr[Decoder[A]] = { // scalastyle:ignore method.length
    val tpe = weakTypeOf[A]

    membersFromPrimaryCtor(tpe, rawRequired = true)
      .fold(c.abort(c.enclosingPosition, s"could not find the primary constructor of $tpe")) {
        case (members, _) =>
          val repr = ProductRepr(members)

          val instanceDefs = repr.instances.map(_.decoder).map {
            case instance @ Instance(_, instanceType, instanceName) =>
              q"""
              val $instanceName: _root_.serde.sjs.Decoder[$instanceType] =
                ${instance.resolve()}
            """
          }

          val membersWithNames = repr.members.map { member =>
            (member, TermName(c.freshName("member")))
          }

          val body = membersWithNames.reverse.foldLeft[Tree](q"""
            new _root_.scala.Right[_root_.java.lang.Throwable, $tpe](
              new $tpe(..${membersWithNames.map(_._2)})(any)
            ): _root_.serde.sjs.Decoder.Result[$tpe]
          """) {
            case (accumulated, (Member(_, memberDecodedName, memberType), memberResultName)) =>
              val resultName = TermName(c.freshName("result"))

              q"""
              val $resultName: _root_.serde.sjs.Decoder.Result[$memberType] =
                ${repr.decoder(memberType).name}(dynamic.selectDynamic($memberDecodedName))

              if ($resultName.isRight) {
                val $memberResultName: $memberType = $resultName
                  .asInstanceOf[_root_.scala.Right[_root_.java.lang.Throwable, $memberType]]
                  .value
                $accumulated
              } else {
                $resultName.asInstanceOf[_root_.serde.sjs.Decoder.Result[$tpe]]
              }
            """
          }

          c.Expr[Decoder[A]](q"""
            _root_.serde.sjs.Decoder.instance { any =>
              val dynamic = any.asInstanceOf[_root_.scala.scalajs.js.Dynamic]
              ..$instanceDefs
              $body
            }
          """)
      }
  }

  private[this] def membersFromPrimaryCtorS(tpe: Type,
                                            rawTpe: Type,
                                            rawRequired: Boolean): Option[(List[Member], Option[RawMember])] = {
    tpe.decls.collectFirst {
      case primaryCtor: MethodSymbol if primaryCtor.isPrimaryConstructor =>
        val paramLists = primaryCtor.paramLists
        val members    = membersFromParamList(paramLists.head, tpe)

        val rawMember = if (rawRequired) {
          if (paramLists.size <= 1) {
            c.abort(c.enclosingPosition, "the second parameter list doesn't exist")
          }

          if (paramLists.tail.head.size != 1) {
            c.abort(c.enclosingPosition, "the second parameter list must have exactly one parameter")
          }

          val member = membersFromParamList(paramLists.tail.head, tpe).head

          if (!(member.tpe =:= rawTpe)) {
            c.abort(c.enclosingPosition, s"the only parameter of the second parameter list must be of type ${rawTpe}")
          }

          Some(RawMember(member.name, member.decodedName))
        } else {
          None
        }

        (members, rawMember)
    }
  }

  def hocEffect[K: c.WeakTypeTag, F <: js.Object: c.WeakTypeTag, E: c.WeakTypeTag]
    : c.Expr[HocEffect[K, F, E]] = { // scalastyle:ignore method.length
    val ktpe = weakTypeOf[K]
    val ftpe = weakTypeOf[F]
    val etpe = weakTypeOf[E]

    val kSymbol = weakTypeTag[K].tpe.typeSymbol

    if (!kSymbol.isClass || !kSymbol.asClass.isCaseClass) {
      c.abort(c.enclosingPosition, s"${kSymbol.fullName} must be a case class")
    }

    val firstParam = membersFromPrimaryCtorS(ktpe, ftpe, true).fold(
      c.abort(c.enclosingPosition, s"could not find the primary constructor of $ktpe, or it's not a case class.")) {
      case (members, Some(rawMember)) =>
        q"_.${rawMember.name}"
      case (_, None) => c.abort(c.enclosingPosition, s"expected a second parameter raw member of type `$ftpe`")
    }

    c.Expr[HocEffect[K, F, E]](q"""
       _root_.serde.sjs.HocEffect.instance[$ktpe, $ftpe, $etpe]($firstParam, _.copy()(_))
     """)
  }
}
