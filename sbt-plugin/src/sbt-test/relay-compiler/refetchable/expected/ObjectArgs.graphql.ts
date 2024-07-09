/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type Thing = {
    stuff: Stuff;
};
export type Stuff = {
    junk?: string | null;
};
export type ObjectArgsVariables = {
    thing?: Thing | null;
};
export type ObjectArgsResponse = {
    readonly " $fragmentRefs": FragmentRefs<"Test_fragment4">;
};
export type ObjectArgs = {
    readonly response: ObjectArgsResponse;
    readonly variables: ObjectArgsVariables;
};



/*
query ObjectArgs(
  $thing: Thing
) {
  ...Test_fragment4
}

fragment Test_fragment4 on Query {
  objArg(thing: $thing) {
    bar
  }
}
*/

const node: ConcreteRequest = (function(){
var v0 = [
  {
    "defaultValue": null,
    "kind": "LocalArgument",
    "name": "thing"
  }
];
return {
  "fragment": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Fragment",
    "metadata": null,
    "name": "ObjectArgs",
    "selections": [
      {
        "args": null,
        "kind": "FragmentSpread",
        "name": "Test_fragment4"
      }
    ],
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": (v0/*: any*/),
    "kind": "Operation",
    "name": "ObjectArgs",
    "selections": [
      {
        "alias": null,
        "args": [
          {
            "kind": "Variable",
            "name": "thing",
            "variableName": "thing"
          }
        ],
        "concreteType": "Foo",
        "kind": "LinkedField",
        "name": "objArg",
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
    "cacheID": "959207d01f7996ad26effede22e447e8",
    "id": null,
    "metadata": {},
    "name": "ObjectArgs",
    "operationKind": "query",
    "text": "query ObjectArgs(\n  $thing: Thing\n) {\n  ...Test_fragment4\n}\n\nfragment Test_fragment4 on Query {\n  objArg(thing: $thing) {\n    bar\n  }\n}\n"
  }
};
})();
(node as any).hash = '87e971b5a1eac4f3835cbac96f3635a8';
export default node;
