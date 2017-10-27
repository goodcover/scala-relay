const DictionaryWordFragment = graphql`
fragment DictionaryComponent_word on Word {
  id
  definition {
    ...DictionaryComponent_definition
  }
}
`

const DictionaryDefinitionFragment = graphql`
fragment DictionaryComponent_definition on WordDefinition {
  text
  image
}
`