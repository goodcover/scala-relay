package example

@graphql("""
    query TestQuery2 {
      defaultSettings {
        notificationSounds
        cache_id
      }
    }
  """)
object Test2 {}
