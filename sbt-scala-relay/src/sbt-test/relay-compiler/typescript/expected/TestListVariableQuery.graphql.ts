/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type TestListVariableQueryVariables = {
    as: Array<string>;
};
export type TestListVariableQueryResponse = {
    readonly listVariable: {
        readonly id: string;
    };
};
export type TestListVariableQuery = {
    readonly response: TestListVariableQueryResponse;
    readonly variables: TestListVariableQueryVariables;
};



/*
query TestListVariableQuery(
  $as: [String!]!
) {
  listVariable(as: $as) {
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
    "name": "as"
  }
],
v1 = [
  {
    "kind": "Variable",
    "name": "as",
    "variableName": "as"
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
    "name": "TestListVariableQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "listVariable",
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
    "name": "TestListVariableQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "listVariable",
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
    "cacheID": "738c663f5b699cf3e6e61df8a4bc4c24",
    "id": null,
    "metadata": {},
    "name": "TestListVariableQuery",
    "operationKind": "query",
    "text": "query TestListVariableQuery(\n  $as: [String!]!\n) {\n  listVariable(as: $as) {\n    __typename\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = 'ebd4539df189116a92a9b405570b78d2';
export default node;
