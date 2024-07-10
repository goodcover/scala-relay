/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ReaderFragment } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type Test_fragment6 = {
    readonly foo: {
        readonly args: {
            readonly " $fragmentRefs": FragmentRefs<"Test_args">;
        } | null;
    };
    readonly " $refType": "Test_fragment6";
};
export type Test_fragment6$data = Test_fragment6;
export type Test_fragment6$key = {
    readonly " $data"?: Test_fragment6$data;
    readonly " $fragmentRefs": FragmentRefs<"Test_fragment6">;
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
      "operation": require('./ArgsOfSpread.graphql.ts')
    }
  },
  "name": "Test_fragment6",
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
              "args": null,
              "kind": "FragmentSpread",
              "name": "Test_args"
            }
          ],
          "storageKey": null
        }
      ],
      "storageKey": null
    }
  ],
  "type": "Query",
  "abstractKey": null
};
(node as any).hash = '84997b7881dcabc2c00f72b7c3728786';
export default node;
