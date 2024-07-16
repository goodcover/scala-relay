/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type TestPrimitiveResponseQueryVariables = {
    a: string;
};
export type TestPrimitiveResponseQueryResponse = {
    readonly primitiveResponse: string;
};
export type TestPrimitiveResponseQuery = {
    readonly response: TestPrimitiveResponseQueryResponse;
    readonly variables: TestPrimitiveResponseQueryVariables;
};



/*
query TestPrimitiveResponseQuery(
  $a: String!
) {
  primitiveResponse(a: $a)
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
    "kind": "ScalarField",
    "name": "primitiveResponse",
    "storageKey": null
  }
];
return {
  "fragment": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Fragment",
    "metadata": null,
    "name": "TestPrimitiveResponseQuery",
    "selections": (v1/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "TestPrimitiveResponseQuery",
    "selections": (v1/*: any*/)
  },
  "params": {
    "cacheID": "8d92b97a3db25e7193bf967ac97b3767",
    "id": null,
    "metadata": {},
    "name": "TestPrimitiveResponseQuery",
    "operationKind": "query",
    "text": "query TestPrimitiveResponseQuery(\n  $a: String!\n) {\n  primitiveResponse(a: $a)\n}\n"
  }
};
})();
(node as any).hash = 'ae5f3e970bf37447206d4db3f9a469a6';
export default node;
