/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type TestListResponseQueryVariables = {
    a: string;
};
export type TestListResponseQueryResponse = {
    readonly listResponse: ReadonlyArray<string>;
};
export type TestListResponseQuery = {
    readonly response: TestListResponseQueryResponse;
    readonly variables: TestListResponseQueryVariables;
};



/*
query TestListResponseQuery(
  $a: String!
) {
  listResponse(a: $a)
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
    "name": "listResponse",
    "storageKey": null
  }
];
return {
  "fragment": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Fragment",
    "metadata": null,
    "name": "TestListResponseQuery",
    "selections": (v1/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "TestListResponseQuery",
    "selections": (v1/*: any*/)
  },
  "params": {
    "cacheID": "c509b0d4521c30896ea6581cfff9b666",
    "id": null,
    "metadata": {},
    "name": "TestListResponseQuery",
    "operationKind": "query",
    "text": "query TestListResponseQuery(\n  $a: String!\n) {\n  listResponse(a: $a)\n}\n"
  }
};
})();
(node as any).hash = '8c2828c15f3c64a05bd6e068053eec1a';
export default node;
