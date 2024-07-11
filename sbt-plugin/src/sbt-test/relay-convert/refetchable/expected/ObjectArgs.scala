package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
*/

trait ObjectArgsInput extends js.Object {
  val thing: ObjectArgsThing | Null
}

trait ObjectArgsThing extends js.Object {
  val stuff: ObjectArgsStuff
}

object ObjectArgsThing {
  def apply(
    stuff: ObjectArgsStuff
  ): ObjectArgsThing =
    js.Dynamic.literal(
      "stuff" -> stuff.asInstanceOf[js.Any]
    ).asInstanceOf[ObjectArgsThing]
}

trait ObjectArgsStuff extends js.Object {
  val junk: String | Null
}

object ObjectArgsStuff {
  def apply(
    junk: String | Null = null
  ): ObjectArgsStuff =
    js.Dynamic.literal(
      "junk" -> junk.asInstanceOf[js.Any]
    ).asInstanceOf[ObjectArgsStuff]
}

@js.native
trait ObjectArgs extends js.Object

object ObjectArgs extends _root_.relay.gql.QueryTaggedNode[ObjectArgsInput, ObjectArgs] {
  type Ctor[T] = T

  implicit class ObjectArgs2Test_fragment4Ref(f: ObjectArgs) extends _root_.relay.gql.CastToFragmentRef[ObjectArgs, Test_fragment4](f) {
    def toTest_fragment4: _root_.relay.gql.FragmentRef[Test_fragment4] = castToRef
  }

  def newInput(
    thing: ObjectArgsThing | Null = null
  ): ObjectArgsInput =
    js.Dynamic.literal(
      "thing" -> thing.asInstanceOf[js.Any]
    ).asInstanceOf[ObjectArgsInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("__generated/ObjectArgs.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
