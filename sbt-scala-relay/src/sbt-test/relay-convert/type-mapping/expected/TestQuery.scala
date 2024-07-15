package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|
import _root_.scala.scalajs.js.annotation.JSImport

/*
query TestQuery($input: ClientTypeInput!) {
    clientType(input: $input) {
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
}
*/

trait TestQueryInput extends js.Object {
  val input: ClientTypeInput
}

object TestQueryInput {
  def apply(
    input: ClientTypeInput
  ): TestQueryInput =
    js.Dynamic.literal(
      "input" -> input.asInstanceOf[js.Any]
    ).asInstanceOf[TestQueryInput]
}

@js.native
trait TestQuery extends js.Object {
  val clientType: TestQuery.ClientType | Null
}

object TestQuery extends _root_.com.goodcover.relay.QueryTaggedNode[TestQueryInput, TestQuery] {
  type Ctor[T] = T

  @js.native
  trait ClientTypeObject extends js.Object {
    val required: Bar
  }

  @js.native
  trait ClientTypeInterface extends js.Object {
    val required: Bar
  }

  @js.native
  trait ClientType extends js.Object {
    val id: String | Null
    val number: Double | Null
    val required: Bar
    val optional: Bar | Null
    val requiredListRequiredElements: js.Array[Bar]
    val requiredListOptionalElements: js.Array[Bar | Null]
    val optionalListRequiredElements: js.Array[Bar] | Null
    val optionalListOptionalElements: js.Array[Bar | Null] | Null
    val `object`: ClientTypeObject
    val interface: ClientTypeInterface
  }

  def newInput(
    input: _root_.relay.generated.ClientTypeInput
  ): _root_.relay.generated.TestQueryInput =
    _root_.relay.generated.TestQueryInput(
      input
    )

  type Query = _root_.com.goodcover.relay.ConcreteRequest

  @js.native
  @JSImport("__generated__/TestQuery.graphql", JSImport.Default)
  private object node extends js.Object

  lazy val query: Query = node.asInstanceOf[Query]

  lazy val sourceHash: String = node.asInstanceOf[js.Dynamic].hash.asInstanceOf[String]
}
