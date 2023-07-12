package relay.runtime

import relay.gql.{FragmentRef, ReaderInlineDataFragment, TaggedNodeQuery, Inline}

import scala.annotation.unchecked.uncheckedVariance
import scala.scalajs.js
import scala.scalajs.js.|
import scala.scalajs.js.annotation.{JSImport, JSName}

object hooks {
  type Id[+A]     = A
  type OrNull[+A] = (A @uncheckedVariance) | Null

  @js.native
  @JSImport("react-relay/hooks", JSImport.Namespace)
  object raw extends js.Object {
    @JSName("useFragment")
    def useFragmentId[O](query: TaggedNodeQuery[Id, O, Any], input: FragmentRef[O]): O = js.native

    @JSName("useFragment")
    def useFragmentOrNull[O](query: TaggedNodeQuery[Id, O, Any], input: OrNull[FragmentRef[O]]): OrNull[O] = js.native

    @JSName("useFragment")
    def useFragmentArray[O](query: TaggedNodeQuery[js.Array, O, Any], input: js.Array[FragmentRef[O]]): js.Array[O] =
      js.native

    @JSName("useFragment")
    def useFragmentOrNullArray[O](
      query: TaggedNodeQuery[js.Array, O, Any],
      input: OrNull[js.Array[FragmentRef[O]]]
    ): OrNull[js.Array[O]] = js.native

    @JSName("readInline")
    def readInlineId[O](query: TaggedNodeQuery[Id, O, Inline], input: FragmentRef[O]): O = js.native

    @JSName("readInline")
    def readInlineOrNull[O](query: TaggedNodeQuery[Id, O, Inline], input: OrNull[FragmentRef[O]]): OrNull[O] =
      js.native

    @JSName("readInline")
    def readInlineArray[O](
      query: TaggedNodeQuery[js.Array, O, Inline],
      input: js.Array[FragmentRef[O]]
    ): js.Array[O] =
      js.native

    @JSName("readInline")
    def readInlineOrNullArray[O](
      query: TaggedNodeQuery[js.Array, O, Inline],
      input: OrNull[js.Array[FragmentRef[O]]]
    ): OrNull[js.Array[O]] = js.native
  }
}
