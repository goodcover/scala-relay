package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
*/

trait ImplicitNodeArgsInput extends js.Object {
  val id: String
}

@js.native
trait ImplicitNodeArgs extends js.Object

object ImplicitNodeArgs extends _root_.relay.gql.QueryTaggedNode[ImplicitNodeArgsInput, ImplicitNodeArgs] {
  type Ctor[T] = T

  implicit class ImplicitNodeArgs2Test_fragment5Ref(f: ImplicitNodeArgs) extends _root_.relay.gql.CastToFragmentRef[ImplicitNodeArgs, Test_fragment5](f) {
    def toTest_fragment5: _root_.relay.gql.FragmentRef[Test_fragment5] = castToRef
  }

  def newInput(
    id: String
  ): ImplicitNodeArgsInput =
    js.Dynamic.literal(
      "id" -> id.asInstanceOf[js.Any]
    ).asInstanceOf[ImplicitNodeArgsInput]

  type Query = _root_.relay.gql.ConcreteRequest

  @js.native
  @JSImport("./__generated__/ImplicitNodeArgs.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
