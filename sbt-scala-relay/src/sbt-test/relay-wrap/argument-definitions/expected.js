graphql`
fragment Test_root on Root @argumentDefinitions(searchTerm: {type: "String"}, before: {type: "String"}, last: {type: "Int"}, after: {type: "String"}, first: {type: "Int"}) {
  viewer {
    things(searchTerm: $searchTerm, before: $before, last: $last, after: $after, first: $first) {
      edges {
        node {
          text
        }
      }
      pageInfo {
        startCursor
        hasNextPage
        endCursor
        hasPreviousPage
      }
    }
  }
}`

