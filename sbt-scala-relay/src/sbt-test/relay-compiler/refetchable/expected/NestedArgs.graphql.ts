/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type NestedArgsVariables = {
    b?: string | null;
};
export type NestedArgsResponse = {
    readonly " $fragmentRefs": FragmentRefs<"Test_fragment3">;
};
export type NestedArgs = {
    readonly response: NestedArgsResponse;
    readonly variables: NestedArgsVariables;
};



/*
query NestedArgs(
  $b: String
) {
  ...Test_fragment3
}

fragment Test_fragment3 on Query {
  foo {
    args {
      oneArg(a: $b) {
        bar
      }
    }
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
    "name": "NestedArgs",
    "selections": [
      {
        "args": null,
        "kind": "FragmentSpread",
        "name": "Test_fragment3"
      }
    ],
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "NestedArgs",
    "selections": [
      {
        "alias": null,
        "args": null,
        "concreteType": "Foo",
        "kind": "LinkedField",
        "name": "foo",
        "plural": false,
        "selections": [
          {
            "alias": null,
            "args": null,
            "concreteType": "Args",
            "kind": "LinkedField",
            "name": "args",
            "plural": false,
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
            ],
            "storageKey": null
          }
        ],
        "storageKey": null
      }
    ]
  },
  "params": {
    "cacheID": "104bf43cc46512e6a7d72290e4a54f78",
    "id": null,
    "metadata": {},
    "name": "NestedArgs",
    "operationKind": "query",
    "text": "query NestedArgs(\n  $b: String\n) {\n  ...Test_fragment3\n}\n\nfragment Test_fragment3 on Query {\n  foo {\n    args {\n      oneArg(a: $b) {\n        bar\n      }\n    }\n  }\n}\n"
  }
};
})();
(node as any).hash = '938c9e5ece2cd64746648535db50d3f0';
export default node;
