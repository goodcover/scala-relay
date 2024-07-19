/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type TestNoVariablesQueryVariables = {};
export type TestNoVariablesQueryResponse = {
    readonly noVariables: {
        readonly id: string;
    };
};
export type TestNoVariablesQuery = {
    readonly response: TestNoVariablesQueryResponse;
    readonly variables: TestNoVariablesQueryVariables;
};



/*
query TestNoVariablesQuery {
  noVariables {
    __typename
    id
  }
}
*/

const node: ConcreteRequest = (function(){
var v0 = {
  "alias": null,
  "args": null,
  "kind": "ScalarField",
  "name": "id",
  "storageKey": null
};
return {
  "fragment": {
    "argumentDefinitions": [],
    "kind": "Fragment",
    "metadata": null,
    "name": "TestNoVariablesQuery",
    "selections": [
      {
        "alias": null,
        "args": null,
        "concreteType": null,
        "kind": "LinkedField",
        "name": "noVariables",
        "plural": false,
        "selections": [
          (v0/*: any*/)
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
    "name": "TestNoVariablesQuery",
    "selections": [
      {
        "alias": null,
        "args": null,
        "concreteType": null,
        "kind": "LinkedField",
        "name": "noVariables",
        "plural": false,
        "selections": [
          {
            "alias": null,
            "args": null,
            "kind": "ScalarField",
            "name": "__typename",
            "storageKey": null
          },
          (v0/*: any*/)
        ],
        "storageKey": null
      }
    ]
  },
  "params": {
    "cacheID": "ccbb9e9444e304c2861f66b66321eaf2",
    "id": null,
    "metadata": {},
    "name": "TestNoVariablesQuery",
    "operationKind": "query",
    "text": "query TestNoVariablesQuery {\n  noVariables {\n    __typename\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = 'dac92ce0b34a88e1bd5a33f34d726fa1';
export default node;
