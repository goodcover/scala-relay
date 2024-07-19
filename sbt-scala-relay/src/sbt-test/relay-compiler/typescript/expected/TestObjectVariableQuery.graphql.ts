/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type Input = {
    a: string;
};
export type TestObjectVariableQueryVariables = {
    input: Input;
};
export type TestObjectVariableQueryResponse = {
    readonly objectVariable: {
        readonly id: string;
    };
};
export type TestObjectVariableQuery = {
    readonly response: TestObjectVariableQueryResponse;
    readonly variables: TestObjectVariableQueryVariables;
};



/*
query TestObjectVariableQuery(
  $input: Input!
) {
  objectVariable(input: $input) {
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
    "name": "input"
  }
],
v1 = [
  {
    "kind": "Variable",
    "name": "input",
    "variableName": "input"
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
    "name": "TestObjectVariableQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "objectVariable",
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
    "name": "TestObjectVariableQuery",
    "selections": [
      {
        "alias": null,
        "args": (v1/*: any*/),
        "concreteType": null,
        "kind": "LinkedField",
        "name": "objectVariable",
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
    "cacheID": "b57eec83e26769b68e5f2b4b2af1334f",
    "id": null,
    "metadata": {},
    "name": "TestObjectVariableQuery",
    "operationKind": "query",
    "text": "query TestObjectVariableQuery(\n  $input: Input!\n) {\n  objectVariable(input: $input) {\n    __typename\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = '21a8512b95c125022450a8d665aaac29';
export default node;
