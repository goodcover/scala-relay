graphql`
query TestQuery {
  defaultSettings {
    notificationSounds
    cache_id
  }
}`

graphql`
fragment Test_foo on Foo {
  bar {
    baz
  }
}`

