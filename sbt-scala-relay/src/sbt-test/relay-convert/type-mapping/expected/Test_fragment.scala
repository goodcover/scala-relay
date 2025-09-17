package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
fragment Test_fragment on ClientTypeOutput {
    id
    number
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
  val id: String | Null
  val number: Double | Null
  val required: Bar
  val optional: Bar | Null
  val requiredListRequiredElements: js.Array[Bar]
  val requiredListOptionalElements: js.Array[Bar | Null]
  val optionalListRequiredElements: js.Array[Bar] | Null
  val optionalListOptionalElements: js.Array[Bar | Null] | Null
  val `object`: Test_fragment.Object
  val interface: Test_fragment.Interface
}

object Test_fragment extends _root_.com.goodcover.relay.FragmentTaggedNode[Test_fragment] {
  type Ctor[T] = T

  @js.native
  trait Object extends js.Object {
    val required: Bar
  }

  @js.native
  trait Interface extends js.Object {
    val required: Bar
  }

  type Query = _root_.com.goodcover.relay.ReaderFragment[Ctor, Out]

  @js.native
  @JSImport("__generated__/Test_fragment.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
