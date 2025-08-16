package com.example.test

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object TestQueries {
  
  @graphql("""
    query GetUser($id: ID!) {
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
    query GetUsers($limit: Int, $offset: Int) {
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
    query GetPost($id: ID!) {
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
    mutation CreateUser($input: CreateUserInput!) {
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
    mutation UpdateUser($id: ID!, $input: UpdateUserInput!) {
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
    mutation CreatePost($input: CreatePostInput!) {
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
    fragment UserInfo on User {
      id
      name
      email
      createdAt
    }
  """)
  def userInfoFragment: js.Object = js.native
  
  @graphql("""
    fragment PostInfo on Post {
      id
      title
      content
      tags
      createdAt
      author {
        ...UserInfo
      }
    }
  """)
  def postInfoFragment: js.Object = js.native
}

object TestSubscriptions {
  
  @graphql("""
    subscription OnUserCreated {
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
    subscription OnPostCreated {
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
    query GetUserWithMacro($id: ID!) {
      user(id: $id) {
        id
        name
        email
      }
    }
  """)
  
  val createUserWithMacro = graphqlGen("""
    mutation CreateUserWithMacro($input: CreateUserInput!) {
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
    query GetUserWithPosts($id: ID!) {
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
    query SearchContent($query: String!) {
      search(query: $query) {
        ... on User {
          id
          name
          email
        }
        ... on Post {
          id
          title
          content
          author {
            id
            name
          }
        }
      }
    }
  """)
  def searchContentQuery: js.Object = js.native
}
