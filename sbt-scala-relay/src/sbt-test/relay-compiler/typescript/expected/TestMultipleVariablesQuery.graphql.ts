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
    "alias": null,
    "args": [
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
    "concreteType": "Node",
    "kind": "LinkedField",
    "name": "multipleVariables",
    "plural": false,
    "selections": [
      {
        "alias": null,
        "args": null,
        "kind": "ScalarField",
        "name": "id",
        "storageKey": null
      }
    ],
    "storageKey": null
  }
];
return {
  "fragment": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Fragment",
    "metadata": null,
    "name": "TestMultipleVariablesQuery",
    "selections": (v1/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "TestMultipleVariablesQuery",
    "selections": (v1/*: any*/)
  },
  "params": {
    "cacheID": "f3583e1b46be02d1bc9b5d7b9df9964e",
    "id": null,
    "metadata": {},
    "name": "TestMultipleVariablesQuery",
    "operationKind": "query",
    "text": "query TestMultipleVariablesQuery(\n  $a: String!\n  $b: String!\n) {\n  multipleVariables(a: $a, b: $b) {\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = '8996cb53f81b433e29e54a504116b79e';
export default node;
