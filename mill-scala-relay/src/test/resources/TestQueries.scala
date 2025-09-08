package com.example.test

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object TestQueries {

  @graphql("""
    query TestQueriesGetUserQuery($id: ID!) {
      user(id: $id) {
        id
        name
        email
        posts {
          id
          title
          content
        }
      }
    }
  """)
  def getUserQuery: js.Object = js.native

  @graphql("""
    query TestQueriesGetUsersQuery {
      users {
        id
        name
        email
        createdAt
        posts {
          id
          title
        }
      }
    }
  """)
  def getUsersQuery: js.Object = js.native

  @graphql("""
    query TestQueriesGetPostQuery($id: ID!) {
      post(id: $id) {
        id
        title
        content
        author {
          id
          name
          email
        }
        tags
        createdAt
      }
    }
  """)
  def getPostQuery: js.Object = js.native
}

object TestMutations {

  @graphql("""
    mutation TestQueriesCreateUserMutation($input: CreateUserInput!) {
      createUser(input: $input) {
        id
        name
        email
        createdAt
      }
    }
  """)
  def createUserMutation: js.Object = js.native

  @graphql("""
    mutation TestQueriesUpdateUserMutation($id: ID!, $input: UpdateUserInput!) {
      updateUser(id: $id, input: $input) {
        id
        name
        email
        updatedAt
      }
    }
  """)
  def updateUserMutation: js.Object = js.native

  @graphql("""
    mutation TestQueriesCreatePostMutation($input: CreatePostInput!) {
      createPost(input: $input) {
        id
        title
        content
        author {
          id
          name
        }
        tags
        createdAt
      }
    }
  """)
  def createPostMutation: js.Object = js.native
}

object TestFragments {

  @graphql("""
    fragment TestQueriesUserInfo on User {
      id
      name
      email
      createdAt
    }
  """)
  def userInfoFragment: js.Object = js.native

  @graphql("""
    fragment TestQueriesPostInfo on Post {
      id
      title
      content
      tags
      createdAt
      author {
        ...TestQueriesUserInfo
      }
    }
  """)
  def postInfoFragment: js.Object = js.native
}

object TestSubscriptions {

  @graphql("""
    subscription TestQueriesOnUserCreatedSubscription {
      userCreated {
        id
        name
        email
        createdAt
      }
    }
  """)
  def onUserCreatedSubscription: js.Object = js.native

  @graphql("""
    subscription TestQueriesOnPostCreatedSubscription {
      postCreated {
        id
        title
        author {
          id
          name
        }
        createdAt
      }
    }
  """)
  def onPostCreatedSubscription: js.Object = js.native
}

// Test with graphqlGen macro calls
object TestMacros {

  val getUserWithMacro = graphqlGen("""
    query TestQueriesGetUserWithMacroQuery($id: ID!) {
      user(id: $id) {
        id
        name
        email
      }
    }
  """)

  val createUserWithMacro = graphqlGen("""
    mutation TestQueriesCreateUserWithMacroMutation($input: CreateUserInput!) {
      createUser(input: $input) {
        id
        name
        email
      }
    }
  """)
}

// Test with complex nested selections
object TestComplexQueries {

  @graphql("""
    query TestQueriesGetUserWithPostsQuery($id: ID!) {
      user(id: $id) {
        id
        name
        email
        posts {
          id
          title
          content
          author {
            id
            name
          }
          ... on Post {
            tags
            createdAt
          }
        }
      }
    }
  """)
  def getUserWithPostsQuery: js.Object = js.native

  @graphql("""
    query TestQueriesGetAllPostsQuery {
      posts {
        id
        title
        content
        author {
          id
          name
          email
        }
        tags
        createdAt
      }
    }
  """)
  def getAllPostsQuery: js.Object = js.native
}
