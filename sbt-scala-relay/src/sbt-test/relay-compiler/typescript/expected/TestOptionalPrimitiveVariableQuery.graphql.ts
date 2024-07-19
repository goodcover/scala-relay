/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type TestOptionalPrimitiveVariableQueryVariables = {
    a?: string | null;
};
export type TestOptionalPrimitiveVariableQueryResponse = {
    readonly primitiveVariable: {
        readonly id: string;
    };
};
export type TestOptionalPrimitiveVariableQuery = {
    readonly response: TestOptionalPrimitiveVariableQueryResponse;
    readonly variables: TestOptionalPrimitiveVariableQueryVariables;
};



/*
query TestOptionalPrimitiveVariableQuery(
  $a: String
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
    "name": "TestOptionalPrimitiveVariableQuery",
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
    "name": "TestOptionalPrimitiveVariableQuery",
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
    "cacheID": "1504e6ac28226b64b19fd74a1a8f418a",
    "id": null,
    "metadata": {},
    "name": "TestOptionalPrimitiveVariableQuery",
    "operationKind": "query",
    "text": "query TestOptionalPrimitiveVariableQuery(\n  $a: String\n) {\n  primitiveVariable(a: $a) {\n    __typename\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = '4fe68166a6f95c9179cfa77ac4a04224';
export default node;
