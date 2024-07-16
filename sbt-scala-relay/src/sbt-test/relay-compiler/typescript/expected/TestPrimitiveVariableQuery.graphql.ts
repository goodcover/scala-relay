/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type TestPrimitiveVariableQueryVariables = {
    a: string;
};
export type TestPrimitiveVariableQueryResponse = {
    readonly primitiveVariable: {
        readonly id: string;
    };
};
export type TestPrimitiveVariableQuery = {
    readonly response: TestPrimitiveVariableQueryResponse;
    readonly variables: TestPrimitiveVariableQueryVariables;
};



/*
query TestPrimitiveVariableQuery(
  $a: String!
) {
  primitiveVariable(a: $a) {
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
      }
    ],
    "concreteType": "Node",
    "kind": "LinkedField",
    "name": "primitiveVariable",
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
    "name": "TestPrimitiveVariableQuery",
    "selections": (v1/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "TestPrimitiveVariableQuery",
    "selections": (v1/*: any*/)
  },
  "params": {
    "cacheID": "94182917396be5d023cb15040da1e88d",
    "id": null,
    "metadata": {},
    "name": "TestPrimitiveVariableQuery",
    "operationKind": "query",
    "text": "query TestPrimitiveVariableQuery(\n  $a: String!\n) {\n  primitiveVariable(a: $a) {\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = 'dbf5ef48f9479ea95c34bedc6448201e';
export default node;
