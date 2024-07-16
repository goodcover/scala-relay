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
    id
  }
}
*/

const node: ConcreteRequest = (function(){
var v0 = [
  {
    "alias": null,
    "args": null,
    "concreteType": "Node",
    "kind": "LinkedField",
    "name": "noVariables",
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
    "argumentDefinitions": [],
    "kind": "Fragment",
    "metadata": null,
    "name": "TestNoVariablesQuery",
    "selections": (v0/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": [],
    "kind": "Operation",
    "name": "TestNoVariablesQuery",
    "selections": (v0/*: any*/)
  },
  "params": {
    "cacheID": "0ce07dd31c974ecc80ead2d604b9c0f6",
    "id": null,
    "metadata": {},
    "name": "TestNoVariablesQuery",
    "operationKind": "query",
    "text": "query TestNoVariablesQuery {\n  noVariables {\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = 'dac92ce0b34a88e1bd5a33f34d726fa1';
export default node;
