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
# Extracted from src/main/scala/example/Test.scala
fragment Test_foo on Foo {
bar {
    baz
 }
  }
`

