/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ReaderFragment } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type Test_fragment3 = {
    readonly foo: {
        readonly args: {
            readonly oneArg: {
                readonly bar: string | null;
            };
        } | null;
    };
    readonly " $refType": "Test_fragment3";
};
export type Test_fragment3$data = Test_fragment3;
export type Test_fragment3$key = {
    readonly " $data"?: Test_fragment3$data;
    readonly " $fragmentRefs": FragmentRefs<"Test_fragment3">;
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
      "operation": require('./NestedArgs.graphql.ts')
    }
  },
  "name": "Test_fragment3",
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
  ],
  "type": "Query",
  "abstractKey": null
};
(node as any).hash = '938c9e5ece2cd64746648535db50d3f0';
export default node;
