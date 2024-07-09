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
    "alias": null,
    "args": [
      {
        "kind": "Variable",
        "name": "nested",
        "variableName": "nested"
      }
    ],
    "concreteType": "Node",
    "kind": "LinkedField",
    "name": "nestedObjectVariable",
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
    "name": "TestNestedObjectVariableQuery",
    "selections": (v1/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "TestNestedObjectVariableQuery",
    "selections": (v1/*: any*/)
  },
  "params": {
    "cacheID": "821c92ae2cfb86f1c8e42afa85a4ea75",
    "id": null,
    "metadata": {},
    "name": "TestNestedObjectVariableQuery",
    "operationKind": "query",
    "text": "query TestNestedObjectVariableQuery(\n  $nested: Nested\n) {\n  nestedObjectVariable(nested: $nested) {\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = 'c735aab5319265c3a7c941ca6cd5087a';
export default node;
