package example2

@graphql("""
    query Test2Query {
      defaultSettings {
        notificationSounds
        cache_id
      }
    }
  """)
object Test2 {

}
