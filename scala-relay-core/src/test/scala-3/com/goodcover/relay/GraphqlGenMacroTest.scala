package com.goodcover.relay

import com.goodcover.relay.macros.graphqlGen
import utest.*
import scala.scalajs.js

class GraphqlGenMacroTest extends TestSuite {

  val tests = Tests {

    test("graphqlGen should work with query") {
      val result = graphqlGen("""
      query TestQuery {
        user {
          id
          name
        }
      }
    """)

      // The macro should generate a reference to relay.generated.TestQuery
      // We can't directly test the type, but we can verify it compiles
      assert(result != null)
    }

    test("graphqlGen should work with mutation") {
      val result = graphqlGen("""
      mutation TestMutation($input: UserInput!) {
        createUser(input: $input) {
          id
          name
        }
      }
    """)

      assert(result != null)
    }

    test("graphqlGen should work with subscription") {
      val result = graphqlGen("""
      subscription TestSubscription {
        userUpdated {
          id
          name
        }
      }
    """)

      assert(result != null)
    }

    test("graphqlGen should work with fragment") {
      val result = graphqlGen("""
      fragment TestFragment on User {
        id
        name
        email
      }
    """)

      assert(result != null)
    }

    test("graphqlGen should work with complex query names") {
      val result = graphqlGen("""
      query UserQuery($id: ID!) {
        user(id: $id) {
          id
          name
          profile {
            avatar
          }
        }
      }
    """)

      assert(result != null)
    }

    test("graphqlGen should work with underscore in names") {
      val result = graphqlGen("""
      fragment UserProfile_fragment on User {
        id
        profile {
          bio
        }
      }
    """)

      assert(result != null)
    }

    // Note: Error cases would cause compilation failures, so we can't easily test them
    // in a unit test. They would be caught at compile time.

    test("graphqlGen should handle whitespace variations") {
      val result1 = graphqlGen("""query   UserQuery { user { id } }""")
      val result2 = graphqlGen("""
      query UserQuery {
        user {
          id
        }
      }
    """)

      assert(result1 != null)
      assert(result2 != null)
    }

    test("graphqlGen should generate correct object references") {
      import relay.generated._

      // Test that the macro generates references to the actual generated objects
      val queryResult = graphqlGen("""
      query TestQuery {
        user { id }
      }
    """)

      // The macro should generate a reference that has the same type as the generated object
      // We can verify this by checking that we can access properties that exist on the generated object
      val testQuery = queryResult.asInstanceOf[js.Dynamic]
      assert(testQuery.sourceHash.asInstanceOf[String] == "test-query-hash")
    }

    test("graphqlGen compilation test with real usage pattern") {
      // This test verifies that the macro works in a realistic usage scenario
      val query = graphqlGen("""
      query UserQuery($id: ID!) {
        user(id: $id) {
          id
          name
          profile {
            avatar
          }
        }
      }
    """)

      // Verify we can use it like a real generated object
      val userQuery = query.asInstanceOf[js.Dynamic]
      assert(userQuery.sourceHash.asInstanceOf[String] == "user-query-hash")
    }
  }
}
