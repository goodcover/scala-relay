/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ReaderFragment } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type Test_fragment = {
    readonly foo: {
        readonly bar: string | null;
    };
    readonly " $refType": "Test_fragment";
};
export type Test_fragment$data = Test_fragment;
export type Test_fragment$key = {
    readonly " $data"?: Test_fragment$data;
    readonly " $fragmentRefs": FragmentRefs<"Test_fragment">;
};



const node: ReaderFragment = {
  "argumentDefinitions": [],
  "kind": "Fragment",
  "metadata": {
    "refetch": {
      "connection": null,
      "fragmentPathInResult": [],
      "operation": require('./NoArgs.graphql.ts')
    }
  },
  "name": "Test_fragment",
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
  ],
  "type": "Query",
  "abstractKey": null
};
(node as any).hash = '00a5e5188f0fc4d7104e262577cadb9a';
export default node;
