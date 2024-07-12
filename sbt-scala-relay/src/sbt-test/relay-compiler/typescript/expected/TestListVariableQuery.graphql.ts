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
    "alias": null,
    "args": [
      {
        "kind": "Variable",
        "name": "as",
        "variableName": "as"
      }
    ],
    "concreteType": "Node",
    "kind": "LinkedField",
    "name": "listVariable",
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
    "name": "TestListVariableQuery",
    "selections": (v1/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "TestListVariableQuery",
    "selections": (v1/*: any*/)
  },
  "params": {
    "cacheID": "bdfbd3c350023c33e3f495ef5208b65d",
    "id": null,
    "metadata": {},
    "name": "TestListVariableQuery",
    "operationKind": "query",
    "text": "query TestListVariableQuery(\n  $as: [String!]!\n) {\n  listVariable(as: $as) {\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = 'ebd4539df189116a92a9b405570b78d2';
export default node;
