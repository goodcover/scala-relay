/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type Nested = {
    input: Input;
};
export type Input = {
    a: string;
};
export type TestNestedObjectVariableQueryVariables = {
    nested?: Nested | null;
};
export type TestNestedObjectVariableQueryResponse = {
    readonly nestedObjectVariable: {
        readonly id: string;
    };
};
export type TestNestedObjectVariableQuery = {
    readonly response: TestNestedObjectVariableQueryResponse;
    readonly variables: TestNestedObjectVariableQueryVariables;
};



/*
query TestNestedObjectVariableQuery(
  $nested: Nested
) {
  nestedObjectVariable(nested: $nested) {
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
    "name": "nested"
  }
],
v1 = [
  {
    "kind": "Variable",
    "name": "nested",
    "variableName": "nested"
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
    "name": "TestNestedObjectVariableQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "nestedObjectVariable",
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
    "name": "TestNestedObjectVariableQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "nestedObjectVariable",
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
    "cacheID": "61d2d5449b0e48535e74dd17e4daed9c",
    "id": null,
    "metadata": {},
    "name": "TestNestedObjectVariableQuery",
    "operationKind": "query",
    "text": "query TestNestedObjectVariableQuery(\n  $nested: Nested\n) {\n  nestedObjectVariable(nested: $nested) {\n    __typename\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = 'c735aab5319265c3a7c941ca6cd5087a';
export default node;
