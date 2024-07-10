/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type ViewerQueryVariables = {};
export type ViewerQueryResponse = {
    readonly viewer: {
        readonly " $fragmentRefs": FragmentRefs<"Test_viewer">;
    } | null;
};
export type ViewerQuery = {
    readonly response: ViewerQueryResponse;
    readonly variables: ViewerQueryVariables;
};



/*
query ViewerQuery {
  viewer {
    ...Test_viewer
  }
}

fragment Test_viewer on Viewer {
  name
}
*/

const node: ConcreteRequest = {
  "fragment": {
    "argumentDefinitions": [],
    "kind": "Fragment",
    "metadata": null,
    "name": "ViewerQuery",
    "selections": [
      {
        "alias": null,
        "args": null,
        "concreteType": "Viewer",
        "kind": "LinkedField",
        "name": "viewer",
        "plural": false,
        "selections": [
          {
            "args": null,
            "kind": "FragmentSpread",
            "name": "Test_viewer"
          }
        ],
        "storageKey": null
      }
    ],
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": [],
    "kind": "Operation",
    "name": "ViewerQuery",
    "selections": [
      {
        "alias": null,
        "args": null,
        "concreteType": "Viewer",
        "kind": "LinkedField",
        "name": "viewer",
        "plural": false,
        "selections": [
          {
            "alias": null,
            "args": null,
            "kind": "ScalarField",
            "name": "name",
            "storageKey": null
          }
        ],
        "storageKey": null
      }
    ]
  },
  "params": {
    "cacheID": "6c6fe5928e1d82074f9e88ca6838ea93",
    "id": null,
    "metadata": {},
    "name": "ViewerQuery",
    "operationKind": "query",
    "text": "query ViewerQuery {\n  viewer {\n    ...Test_viewer\n  }\n}\n\nfragment Test_viewer on Viewer {\n  name\n}\n"
  }
};
(node as any).hash = '16bd6cce9f7608bf2fbc7e4080dc4e63';
export default node;
