graphql`
# Extracted from src/main/scala/example/Test.scala
# This is rad!
query TestQuery {
  defaultSettings {
    notificationSounds
    cache_id
  }
}
`
graphql`
# Extracted from ${PWD}/src/main/scala/example/Test.scala
query TestQuery2 {
  defaultSettings {
    notificationSounds
    cache_id
  }
}
`

