/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type TestMultipleVariablesQueryVariables = {
    a: string;
    b: string;
};
export type TestMultipleVariablesQueryResponse = {
    readonly multipleVariables: {
        readonly id: string;
    };
};
export type TestMultipleVariablesQuery = {
    readonly response: TestMultipleVariablesQueryResponse;
    readonly variables: TestMultipleVariablesQueryVariables;
};



/*
query TestMultipleVariablesQuery(
  $a: String!
  $b: String!
) {
  multipleVariables(a: $a, b: $b) {
    __typename
    id
  }
}
*/

const node: ConcreteRequest = (function(){
var v0 = [
  {
    "defaultValue": null,
    "kind": "LocalArgument",
    "name": "a"
  },
  {
    "defaultValue": null,
    "kind": "LocalArgument",
    "name": "b"
  }
],
v1 = [
  {
    "kind": "Variable",
    "name": "a",
    "variableName": "a"
  },
  {
    "kind": "Variable",
    "name": "b",
    "variableName": "b"
  }
],
v2 = {
  "alias": null,
  "args": null,
  "kind": "ScalarField",
  "name": "id",
  "storageKey": null
};
return {
  "fragment": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Fragment",
    "metadata": null,
    "name": "TestMultipleVariablesQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "multipleVariables",
        "plural": false,
        "selections": [
          (v2/*: any*/)
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
    "name": "TestMultipleVariablesQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "multipleVariables",
        "plural": false,
        "selections": [
          {
            "alias": null,
            "args": null,
            "kind": "ScalarField",
            "name": "__typename",
            "storageKey": null
          },
          (v2/*: any*/)
        ],
        "storageKey": null
      }
    ]
  },
  "params": {
    "cacheID": "a443434c6d4c7fbc04443d83c205066a",
    "id": null,
    "metadata": {},
    "name": "TestMultipleVariablesQuery",
    "operationKind": "query",
    "text": "query TestMultipleVariablesQuery(\n  $a: String!\n  $b: String!\n) {\n  multipleVariables(a: $a, b: $b) {\n    __typename\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = '8996cb53f81b433e29e54a504116b79e';
export default node;
