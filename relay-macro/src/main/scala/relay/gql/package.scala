package relay

package object gql {
  type ReaderFragment[F[_], O] = TaggedNodeQuery[F, O, Any]

  type ReaderInlineDataFragment[F[_], O] = TaggedNodeQuery[F, O, Inline]
}
