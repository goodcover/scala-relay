package example

@graphql("""
    # This is rad!
    query TestQuery {
      defaultSettings {
        notificationSounds
        cache_id
      }
    }
""")
object Test {

  graphqlGen("""

    fragment Test_foo on Foo {
  bar {
        baz
     }
      }


  """)

 compiler.graphqlGen("""
   fragment Test_foo2 on Foo {
     id
   }
 """)
}
