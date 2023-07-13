package relay.runtime

import relay.gql.{FragmentRef, ReaderInlineDataFragment, TaggedNodeQuery, Block, Inline}

import scala.annotation.unchecked.uncheckedVariance
import scala.scalajs.js
import scala.scalajs.js.|
import scala.scalajs.js.annotation.{JSImport, JSName}

object hooks {
  type Id[+A]     = A
  type OrNull[+A] = (A @uncheckedVariance) | Null

  /** `useFragment` and `readInlineData` method definitions.
    *
    * An enumeration of all input combinations these methods accept.
    */
  @js.native
  @JSImport("react-relay/hooks", JSImport.Namespace)
  object raw extends js.Object {
    @JSName("useFragment")
    def useFragmentId[O](query: TaggedNodeQuery[Id, O, Block], input: FragmentRef[O]): O = js.native

    @JSName("useFragment")
    def useFragmentOrNull[O](query: TaggedNodeQuery[Id, O, Block], input: OrNull[FragmentRef[O]]): OrNull[O] = js.native

    @JSName("useFragment")
    def useFragmentArray[O](query: TaggedNodeQuery[js.Array, O, Block], input: js.Array[FragmentRef[O]]): js.Array[O] =
      js.native

    @JSName("useFragment")
    def useFragmentOrNullArray[O](
      query: TaggedNodeQuery[js.Array, O, Block],
      input: OrNull[js.Array[FragmentRef[O]]]
    ): OrNull[js.Array[O]] = js.native

    @JSName("readInlineData")
    def readInlineDataId[O](query: TaggedNodeQuery[Id, O, Inline], input: FragmentRef[O]): O = js.native

    @JSName("readInlineData")
    def readInlineDataOrNull[O](query: TaggedNodeQuery[Id, O, Inline], input: OrNull[FragmentRef[O]]): OrNull[O] =
      js.native

    @JSName("readInlineData")
    def readInlineDataArray[O](
      query: TaggedNodeQuery[js.Array, O, Inline],
      input: js.Array[FragmentRef[O]]
    ): js.Array[O] =
      js.native

    @JSName("readInlineData")
    def readInlineDataOrNullArray[O](
      query: TaggedNodeQuery[js.Array, O, Inline],
      input: OrNull[js.Array[FragmentRef[O]]]
    ): OrNull[js.Array[O]] = js.native
  }
}
