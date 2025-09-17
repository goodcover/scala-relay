package example

import com.goodcover.relay.macros

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

  macros.graphqlGen("""
   fragment Test_foo2 on Foo {
     id
   }
 """)
}
