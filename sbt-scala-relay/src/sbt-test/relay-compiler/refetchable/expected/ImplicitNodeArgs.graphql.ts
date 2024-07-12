/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type ImplicitNodeArgsVariables = {
    id: string;
};
export type ImplicitNodeArgsResponse = {
    readonly node: {
        readonly " $fragmentRefs": FragmentRefs<"Test_fragment5">;
    } | null;
};
export type ImplicitNodeArgs = {
    readonly response: ImplicitNodeArgsResponse;
    readonly variables: ImplicitNodeArgsVariables;
};



/*
query ImplicitNodeArgs(
  $id: ID!
) {
  node(nodeID: $id) {
    __typename
    ...Test_fragment5
    id
  }
}

fragment Test_fragment5 on ImplicitNode {
  __isImplicitNode: __typename
  name
  id
}
*/

const node: ConcreteRequest = (function(){
var v0 = [
  {
    "defaultValue": null,
    "kind": "LocalArgument",
    "name": "id"
  }
],
v1 = [
  {
    "kind": "Variable",
    "name": "nodeID",
    "variableName": "id"
  }
];
return {
  "fragment": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Fragment",
    "metadata": null,
    "name": "ImplicitNodeArgs",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "node",
        "plural": false,
        "selections": [
          {
            "args": null,
            "kind": "FragmentSpread",
            "name": "Test_fragment5"
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
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "ImplicitNodeArgs",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "node",
        "plural": false,
        "selections": [
          {
            "alias": null,
            "args": null,
            "kind": "ScalarField",
            "name": "__typename",
            "storageKey": null
          },
          {
            "alias": null,
            "args": null,
            "kind": "ScalarField",
            "name": "id",
            "storageKey": null
          },
          {
            "kind": "InlineFragment",
            "selections": [
              {
                "alias": null,
                "args": null,
                "kind": "ScalarField",
                "name": "name",
                "storageKey": null
              }
            ],
            "type": "ImplicitNode",
            "abstractKey": "__isImplicitNode"
          }
        ],
        "storageKey": null
      }
    ]
  },
  "params": {
    "cacheID": "aa27685098dd4f883dcc8d4ad0291050",
    "id": null,
    "metadata": {},
    "name": "ImplicitNodeArgs",
    "operationKind": "query",
    "text": "query ImplicitNodeArgs(\n  $id: ID!\n) {\n  node(nodeID: $id) {\n    __typename\n    ...Test_fragment5\n    id\n  }\n}\n\nfragment Test_fragment5 on ImplicitNode {\n  __isImplicitNode: __typename\n  name\n  id\n}\n"
  }
};
})();
(node as any).hash = 'bd12d7f8666e249e5b176f42a53f8f49';
export default node;
