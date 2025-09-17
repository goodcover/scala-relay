package example

@graphql("""
    query TestQuery {
      defaultSettings {
        notificationSounds
        cache_id
      }
    }
  """)
object Test {}
