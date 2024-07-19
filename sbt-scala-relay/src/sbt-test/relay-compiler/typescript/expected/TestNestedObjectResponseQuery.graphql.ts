/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type TestNestedObjectResponseQueryVariables = {};
export type TestNestedObjectResponseQueryResponse = {
    readonly nestedObjectResponse: {
        readonly output: {
            readonly b: string | null;
        } | null;
    } | null;
};
export type TestNestedObjectResponseQuery = {
    readonly response: TestNestedObjectResponseQueryResponse;
    readonly variables: TestNestedObjectResponseQueryVariables;
};



/*
query TestNestedObjectResponseQuery {
  nestedObjectResponse {
    output {
      b
    }
  }
}
*/

const node: ConcreteRequest = (function(){
var v0 = [
  {
    "alias": null,
    "args": null,
    "concreteType": "NestedOutput",
    "kind": "LinkedField",
    "name": "nestedObjectResponse",
    "plural": false,
    "selections": [
      {
        "alias": null,
        "args": null,
        "concreteType": "Output",
        "kind": "LinkedField",
        "name": "output",
        "plural": false,
        "selections": [
          {
            "alias": null,
            "args": null,
            "kind": "ScalarField",
            "name": "b",
            "storageKey": null
          }
        ],
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
    "name": "TestNestedObjectResponseQuery",
    "selections": (v0/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": [],
    "kind": "Operation",
    "name": "TestNestedObjectResponseQuery",
    "selections": (v0/*: any*/)
  },
  "params": {
    "cacheID": "a3eb84d598dea58055a7058a7a71eae2",
    "id": null,
    "metadata": {},
    "name": "TestNestedObjectResponseQuery",
    "operationKind": "query",
    "text": "query TestNestedObjectResponseQuery {\n  nestedObjectResponse {\n    output {\n      b\n    }\n  }\n}\n"
  }
};
})();
(node as any).hash = 'd5ffd04bedca902ff8f1e9554ce81c5e';
export default node;
