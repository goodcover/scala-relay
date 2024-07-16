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
    "alias": null,
    "args": [
      {
        "kind": "Variable",
        "name": "input",
        "variableName": "input"
      }
    ],
    "concreteType": "Node",
    "kind": "LinkedField",
    "name": "objectVariable",
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
    "name": "TestObjectVariableQuery",
    "selections": (v1/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "TestObjectVariableQuery",
    "selections": (v1/*: any*/)
  },
  "params": {
    "cacheID": "d6f2445fbd0cbeb633f27e872a22d5f2",
    "id": null,
    "metadata": {},
    "name": "TestObjectVariableQuery",
    "operationKind": "query",
    "text": "query TestObjectVariableQuery(\n  $input: Input!\n) {\n  objectVariable(input: $input) {\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = '21a8512b95c125022450a8d665aaac29';
export default node;
