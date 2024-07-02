package example2

@graphql("""
    query TestQuery {
      defaultSettings {
        notificationSounds
        cache_id
      }
    }
  """)
object TestQ {

}
