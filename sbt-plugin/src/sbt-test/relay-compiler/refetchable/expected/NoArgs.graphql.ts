/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ConcreteRequest } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type NoArgsVariables = {};
export type NoArgsResponse = {
    readonly " $fragmentRefs": FragmentRefs<"Test_fragment">;
};
export type NoArgs = {
    readonly response: NoArgsResponse;
    readonly variables: NoArgsVariables;
};



/*
query NoArgs {
  ...Test_fragment
}

fragment Test_fragment on Query {
  foo {
    bar
  }
}
*/

const node: ConcreteRequest = {
  "fragment": {
    "argumentDefinitions": [],
    "kind": "Fragment",
    "metadata": null,
    "name": "NoArgs",
    "selections": [
      {
        "args": null,
        "kind": "FragmentSpread",
        "name": "Test_fragment"
      }
    ],
    "type": "Query",
    "abstractKey": null
  },
  "kind": "Request",
  "operation": {
    "argumentDefinitions": [],
    "kind": "Operation",
    "name": "NoArgs",
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
    "cacheID": "10f25a49125758b9353c5eed00cc56e6",
    "id": null,
    "metadata": {},
    "name": "NoArgs",
    "operationKind": "query",
    "text": "query NoArgs {\n  ...Test_fragment\n}\n\nfragment Test_fragment on Query {\n  foo {\n    bar\n  }\n}\n"
  }
};
(node as any).hash = '00a5e5188f0fc4d7104e262577cadb9a';
export default node;
