/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type Input = {
    a: string;
};
export type TestOptionalObjectVariableQueryVariables = {
    input?: Input | null;
};
export type TestOptionalObjectVariableQueryResponse = {
    readonly objectVariable: {
        readonly id: string;
    };
};
export type TestOptionalObjectVariableQuery = {
    readonly response: TestOptionalObjectVariableQueryResponse;
    readonly variables: TestOptionalObjectVariableQueryVariables;
};



/*
query TestOptionalObjectVariableQuery(
  $input: Input
) {
  objectVariable(input: $input) {
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
    "name": "input"
  }
],
v1 = [
  {
    "kind": "Variable",
    "name": "input",
    "variableName": "input"
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
    "name": "TestOptionalObjectVariableQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "objectVariable",
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
    "name": "TestOptionalObjectVariableQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "objectVariable",
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
    "cacheID": "a4de4acf7c3991f740aec1845562df7e",
    "id": null,
    "metadata": {},
    "name": "TestOptionalObjectVariableQuery",
    "operationKind": "query",
    "text": "query TestOptionalObjectVariableQuery(\n  $input: Input\n) {\n  objectVariable(input: $input) {\n    __typename\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = 'bb8909816bd7482776ff3014d7f294e4';
export default node;
