package com.goodcover

package object relay {

  type ReaderFragment[F[_], O] = TaggedNodeQuery[F, O, Block]

  type ReaderInlineDataFragment[F[_], O] = TaggedNodeQuery[F, O, Inline]
}
