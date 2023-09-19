package relay

package object gql {
  type Block = Any

  type ReaderFragment[F[_], O] = TaggedNodeQuery[F, O, Block]

  type ReaderInlineDataFragment[F[_], O] = TaggedNodeQuery[F, O, Inline]
}
