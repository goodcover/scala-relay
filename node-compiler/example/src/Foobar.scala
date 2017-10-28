object Foo {

  val otherGql = @gql("""
  fragment DictionaryComponent_definition on WordDefinition {
    text
    image
  }
  """)

  val gql = @gql("""
  fragment DictionaryComponent_word on Word {
    id
    definition {
      ...DictionaryComponent_definition
      text
      id
    }
  }
  """)

  val query = @gql("""
  query DictionaryQuery {
    dictionary {
      ...DictionaryComponent_word
    }
  }
  """)
}
