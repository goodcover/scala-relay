/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type TestOptionalListVariableQueryVariables = {
    as?: Array<string> | null;
};
export type TestOptionalListVariableQueryResponse = {
    readonly listVariable: {
        readonly id: string;
    };
};
export type TestOptionalListVariableQuery = {
    readonly response: TestOptionalListVariableQueryResponse;
    readonly variables: TestOptionalListVariableQueryVariables;
};



/*
query TestOptionalListVariableQuery(
  $as: [String!]
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
    "name": "TestOptionalListVariableQuery",
    "selections": (v1/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "TestOptionalListVariableQuery",
    "selections": (v1/*: any*/)
  },
  "params": {
    "cacheID": "07f082c03659cab4b4ecf8feb5e6f737",
    "id": null,
    "metadata": {},
    "name": "TestOptionalListVariableQuery",
    "operationKind": "query",
    "text": "query TestOptionalListVariableQuery(\n  $as: [String!]\n) {\n  listVariable(as: $as) {\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = '81c085977ffb9eee2bbe63066ca36d49';
export default node;
