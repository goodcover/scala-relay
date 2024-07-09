/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type TestOptionalPrimitiveResponseQueryVariables = {
    a: string;
};
export type TestOptionalPrimitiveResponseQueryResponse = {
    readonly optionalPrimitiveResponse: string | null;
};
export type TestOptionalPrimitiveResponseQuery = {
    readonly response: TestOptionalPrimitiveResponseQueryResponse;
    readonly variables: TestOptionalPrimitiveResponseQueryVariables;
};



/*
query TestOptionalPrimitiveResponseQuery(
  $a: String!
) {
  optionalPrimitiveResponse(a: $a)
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
    "name": "optionalPrimitiveResponse",
    "storageKey": null
  }
];
return {
  "fragment": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Fragment",
    "metadata": null,
    "name": "TestOptionalPrimitiveResponseQuery",
    "selections": (v1/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "TestOptionalPrimitiveResponseQuery",
    "selections": (v1/*: any*/)
  },
  "params": {
    "cacheID": "56ff9d75611bdc0e01882da423b4bdeb",
    "id": null,
    "metadata": {},
    "name": "TestOptionalPrimitiveResponseQuery",
    "operationKind": "query",
    "text": "query TestOptionalPrimitiveResponseQuery(\n  $a: String!\n) {\n  optionalPrimitiveResponse(a: $a)\n}\n"
  }
};
})();
(node as any).hash = '169a9dc540c99cd91bcf88b7a7e2dbbe';
export default node;
