package com.goodcover.relay

package object gql {
  type ReaderFragment[F[_], O] = TaggedNodeQuery[F, O, Block]

  type ReaderInlineDataFragment[F[_], O] = TaggedNodeQuery[F, O, Inline]
}
