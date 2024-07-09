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
    "alias": null,
    "args": [
      {
        "kind": "Variable",
        "name": "input",
        "variableName": "input"
      }
    ],
    "concreteType": "Node",
    "kind": "LinkedField",
    "name": "objectVariable",
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
    "name": "TestOptionalObjectVariableQuery",
    "selections": (v1/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "TestOptionalObjectVariableQuery",
    "selections": (v1/*: any*/)
  },
  "params": {
    "cacheID": "7bde3ee007526d42c4afab7761bd15fe",
    "id": null,
    "metadata": {},
    "name": "TestOptionalObjectVariableQuery",
    "operationKind": "query",
    "text": "query TestOptionalObjectVariableQuery(\n  $input: Input\n) {\n  objectVariable(input: $input) {\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = 'bb8909816bd7482776ff3014d7f294e4';
export default node;
