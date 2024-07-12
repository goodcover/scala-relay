/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ReaderFragment } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type Test_fragment2 = {
    readonly oneArg: {
        readonly bar: string | null;
    };
    readonly " $refType": "Test_fragment2";
};
export type Test_fragment2$data = Test_fragment2;
export type Test_fragment2$key = {
    readonly " $data"?: Test_fragment2$data;
    readonly " $fragmentRefs": FragmentRefs<"Test_fragment2">;
};



const node: ReaderFragment = {
  "argumentDefinitions": [
    {
      "kind": "RootArgument",
      "name": "b"
    }
  ],
  "kind": "Fragment",
  "metadata": {
    "refetch": {
      "connection": null,
      "fragmentPathInResult": [],
      "operation": require('./Args.graphql.ts')
    }
  },
  "name": "Test_fragment2",
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
  "type": "Query",
  "abstractKey": null
};
(node as any).hash = 'f870ff586b9eccbe9bda3c7eaf535f2d';
export default node;
