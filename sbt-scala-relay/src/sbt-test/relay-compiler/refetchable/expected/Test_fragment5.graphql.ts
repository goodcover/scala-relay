/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ReaderFragment } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type Test_fragment5 = {
    readonly name: string;
    readonly id: string;
    readonly " $refType": "Test_fragment5";
};
export type Test_fragment5$data = Test_fragment5;
export type Test_fragment5$key = {
    readonly " $data"?: Test_fragment5$data;
    readonly " $fragmentRefs": FragmentRefs<"Test_fragment5">;
};



const node: ReaderFragment = {
  "argumentDefinitions": [],
  "kind": "Fragment",
  "metadata": {
    "refetch": {
      "connection": null,
      "fragmentPathInResult": [
        "node"
      ],
      "operation": require('./ImplicitNodeArgs.graphql.ts'),
      "identifierField": "id"
    }
  },
  "name": "Test_fragment5",
  "selections": [
    {
      "alias": null,
      "args": null,
      "kind": "ScalarField",
      "name": "name",
      "storageKey": null
    },
    {
      "alias": null,
      "args": null,
      "kind": "ScalarField",
      "name": "id",
      "storageKey": null
    }
  ],
  "type": "ImplicitNode",
  "abstractKey": "__isImplicitNode"
};
(node as any).hash = 'bd12d7f8666e249e5b176f42a53f8f49';
export default node;
