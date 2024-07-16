/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type ArgsOfSpreadVariables = {
    b?: string | null;
};
export type ArgsOfSpreadResponse = {
    readonly " $fragmentRefs": FragmentRefs<"Test_fragment6">;
};
export type ArgsOfSpread = {
    readonly response: ArgsOfSpreadResponse;
    readonly variables: ArgsOfSpreadVariables;
};



/*
query ArgsOfSpread(
  $b: String
) {
  ...Test_fragment6
}

fragment Test_args on Args {
  oneArg(a: $b) {
    bar
  }
}

fragment Test_fragment6 on Query {
  foo {
    args {
      ...Test_args
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
    "name": "ArgsOfSpread",
    "selections": [
      {
        "args": null,
        "kind": "FragmentSpread",
        "name": "Test_fragment6"
      }
    ],
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "ArgsOfSpread",
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
    "cacheID": "b67812d0ce7d40efe3abe035522d8080",
    "id": null,
    "metadata": {},
    "name": "ArgsOfSpread",
    "operationKind": "query",
    "text": "query ArgsOfSpread(\n  $b: String\n) {\n  ...Test_fragment6\n}\n\nfragment Test_args on Args {\n  oneArg(a: $b) {\n    bar\n  }\n}\n\nfragment Test_fragment6 on Query {\n  foo {\n    args {\n      ...Test_args\n    }\n  }\n}\n"
  }
};
})();
(node as any).hash = '84997b7881dcabc2c00f72b7c3728786';
export default node;
