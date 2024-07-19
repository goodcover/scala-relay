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
  }
],
v1 = [
  {
    "kind": "Variable",
    "name": "a",
    "variableName": "a"
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
    "name": "TestPrimitiveVariableQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "primitiveVariable",
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
    "name": "TestPrimitiveVariableQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "primitiveVariable",
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
    "cacheID": "8f796d0a09436b81c58fbeda4cf3d2f9",
    "id": null,
    "metadata": {},
    "name": "TestPrimitiveVariableQuery",
    "operationKind": "query",
    "text": "query TestPrimitiveVariableQuery(\n  $a: String!\n) {\n  primitiveVariable(a: $a) {\n    __typename\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = 'dbf5ef48f9479ea95c34bedc6448201e';
export default node;
