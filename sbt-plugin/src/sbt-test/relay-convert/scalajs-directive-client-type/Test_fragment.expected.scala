package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment on ClientTypeOutput {
    required
    optional
    requiredListRequiredElements
    requiredListOptionalElements
    optionalListRequiredElements
    optionalListOptionalElements
    object {
        required
    }
    interface {
        required
    }
}
*/

@js.native
trait Test_fragment extends js.Object {
  val required: String[Required]
  val optional: String[Optional] | Null
  val requiredListRequiredElements: js.Array[String[RequiredListRequiredElements]]
  val requiredListOptionalElements: js.Array[String[RequiredListOptionalElements] | Null]
  val optionalListRequiredElements: js.Array[String[OptionalListRequiredElements]] | Null
  val optionalListOptionalElements: js.Array[String[OptionalListOptionalElements] | Null] | Null
  val `object`: Test_fragment.Object
  val interface: Test_fragment.Interface
}

object Test_fragment extends _root_.relay.gql.FragmentTaggedNode[Test_fragment] {
  type Ctor[T] = T

  @js.native
  trait Object extends js.Object {
    val required: String[Object]
  }

  @js.native
  trait Interface extends js.Object {
    val required: String[Interface]
  }

  type Query = _root_.relay.gql.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated/Test_fragment.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
