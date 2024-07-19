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
    "name": "TestOptionalListVariableQuery",
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
    "name": "TestOptionalListVariableQuery",
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
    "cacheID": "8cb009685ccc1785fba4816343ab804d",
    "id": null,
    "metadata": {},
    "name": "TestOptionalListVariableQuery",
    "operationKind": "query",
    "text": "query TestOptionalListVariableQuery(\n  $as: [String!]\n) {\n  listVariable(as: $as) {\n    __typename\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = '81c085977ffb9eee2bbe63066ca36d49';
export default node;
