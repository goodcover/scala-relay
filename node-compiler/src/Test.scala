object Foo {

  val gql = @gql("""
  fragment DictionaryComponent_word on Word {
    id
    definition {
      ...DictionaryComponent_definition
    }
  }
  """)
}
