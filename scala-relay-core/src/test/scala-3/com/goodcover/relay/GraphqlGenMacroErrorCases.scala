package com.goodcover.relay

import com.goodcover.relay.macros.graphqlGen

/**
 * This file demonstrates error cases for the graphqlGen macro. These examples
 * would cause compilation errors if uncommented.
 *
 * The purpose is to document the expected behavior and provide examples for
 * manual testing of error cases.
 */
object GraphqlGenMacroErrorCases {

  // ❌ This would fail: Non-string literal
  // val variable = "query TestQuery { user { id } }"
  // val result1 = graphqlGen(variable)

  // ❌ This would fail: Invalid GraphQL (missing operation name)
  // val result2 = graphqlGen("""
  //   query {
  //     user { id }
  //   }
  // """)

  // ❌ This would fail: Invalid GraphQL (no operation type)
  // val result3 = graphqlGen("""
  //   TestQuery {
  //     user { id }
  //   }
  // """)

  // ❌ This would fail: Empty string
  // val result4 = graphqlGen("")

  // ❌ This would fail: Non-GraphQL content
  // val result5 = graphqlGen("this is not graphql")

  // ✅ These would work:
  val validQuery = graphqlGen("""
    query ValidQuery {
      user { id }
    }
  """)

  val validMutation = graphqlGen("""
    mutation ValidMutation($input: UserInput!) {
      createUser(input: $input) { id }
    }
  """)

  val validFragment = graphqlGen("""
    fragment ValidFragment on User {
      id
      name
    }
  """)

  val validSubscription = graphqlGen("""
    subscription ValidSubscription {
      userUpdated { id }
    }
  """)

  // ✅ Works with various whitespace
  val queryWithWhitespace = graphqlGen("""query    ValidQuery   { user { id } }""")

  // ✅ Works with underscores in names
  val fragmentWithUnderscore = graphqlGen("""
    fragment User_Profile on User {
      profile { bio }
    }
  """)
}
