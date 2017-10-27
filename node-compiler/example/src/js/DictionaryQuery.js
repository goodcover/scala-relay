const DictionaryQuery = graphql`
query DictionaryQuery {
  dictionary {
    ...DictionaryComponent_word
  }
}
`