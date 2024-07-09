/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type ArgsVariables = {
    b?: string | null;
};
export type ArgsResponse = {
    readonly " $fragmentRefs": FragmentRefs<"Test_fragment2">;
};
export type Args = {
    readonly response: ArgsResponse;
    readonly variables: ArgsVariables;
};



/*
query Args(
  $b: String
) {
  ...Test_fragment2
}

fragment Test_fragment2 on Query {
  oneArg(a: $b) {
    bar
  }
}
*/

const node: ConcreteRequest = (function(){
var v0 = [
  {
    "defaultValue": null,
    "kind": "LocalArgument",
    "name": "b"
  }
];
return {
  "fragment": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Fragment",
    "metadata": null,
    "name": "Args",
    "selections": [
      {
        "args": null,
        "kind": "FragmentSpread",
        "name": "Test_fragment2"
      }
    ],
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "Args",
    "selections": [
      {
        "alias": null,
        "args": [
          {
            "kind": "Variable",
            "name": "a",
            "variableName": "b"
          }
        ],
        "concreteType": "Foo",
        "kind": "LinkedField",
        "name": "oneArg",
        "plural": false,
        "selections": [
          {
            "alias": null,
            "args": null,
            "kind": "ScalarField",
            "name": "bar",
            "storageKey": null
          }
        ],
        "storageKey": null
      }
    ]
  },
  "params": {
    "cacheID": "5715aa51880c73d7a65e590fb34a3d6f",
    "id": null,
    "metadata": {},
    "name": "Args",
    "operationKind": "query",
    "text": "query Args(\n  $b: String\n) {\n  ...Test_fragment2\n}\n\nfragment Test_fragment2 on Query {\n  oneArg(a: $b) {\n    bar\n  }\n}\n"
  }
};
})();
(node as any).hash = 'f870ff586b9eccbe9bda3c7eaf535f2d';
export default node;
