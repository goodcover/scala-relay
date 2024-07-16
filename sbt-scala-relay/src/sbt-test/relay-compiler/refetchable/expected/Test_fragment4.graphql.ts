/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ReaderFragment } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type Test_fragment4 = {
    readonly objArg: {
        readonly bar: string | null;
    };
    readonly " $refType": "Test_fragment4";
};
export type Test_fragment4$data = Test_fragment4;
export type Test_fragment4$key = {
    readonly " $data"?: Test_fragment4$data;
    readonly " $fragmentRefs": FragmentRefs<"Test_fragment4">;
};



const node: ReaderFragment = {
  "argumentDefinitions": [
    {
      "kind": "RootArgument",
      "name": "thing"
    }
  ],
  "kind": "Fragment",
  "metadata": {
    "refetch": {
      "connection": null,
      "fragmentPathInResult": [],
      "operation": require('./ObjectArgs.graphql.ts')
    }
  },
  "name": "Test_fragment4",
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
  ],
  "type": "Query",
  "abstractKey": null
};
(node as any).hash = '87e971b5a1eac4f3835cbac96f3635a8';
export default node;
