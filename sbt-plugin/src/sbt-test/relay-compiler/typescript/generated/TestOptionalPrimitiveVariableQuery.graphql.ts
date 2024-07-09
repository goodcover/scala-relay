/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
export type TestOptionalPrimitiveVariableQueryVariables = {
    a?: string | null;
};
export type TestOptionalPrimitiveVariableQueryResponse = {
    readonly primitiveVariable: {
        readonly id: string;
    };
};
export type TestOptionalPrimitiveVariableQuery = {
    readonly response: TestOptionalPrimitiveVariableQueryResponse;
    readonly variables: TestOptionalPrimitiveVariableQueryVariables;
};



/*
query TestOptionalPrimitiveVariableQuery(
  $a: String
) {
  primitiveVariable(a: $a) {
    id
  }
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
    "concreteType": "Node",
    "kind": "LinkedField",
    "name": "primitiveVariable",
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
    "name": "TestOptionalPrimitiveVariableQuery",
    "selections": (v1/*: any*/),
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "TestOptionalPrimitiveVariableQuery",
    "selections": (v1/*: any*/)
  },
  "params": {
    "cacheID": "e3260e365d12e54904a328e80b465cc2",
    "id": null,
    "metadata": {},
    "name": "TestOptionalPrimitiveVariableQuery",
    "operationKind": "query",
    "text": "query TestOptionalPrimitiveVariableQuery(\n  $a: String\n) {\n  primitiveVariable(a: $a) {\n    id\n  }\n}\n"
  }
};
})();
(node as any).hash = '4fe68166a6f95c9179cfa77ac4a04224';
export default node;
