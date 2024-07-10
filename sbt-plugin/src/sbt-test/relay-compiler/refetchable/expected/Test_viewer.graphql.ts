/* tslint:disable */
/* eslint-disable */
// @ts-nocheck

import { ReaderFragment } from "relay-runtime";
import { FragmentRefs as  } from "relay-runtime";
export type Test_viewer = {
    readonly name: string;
    readonly " $refType": "Test_viewer";
};
export type Test_viewer$data = Test_viewer;
export type Test_viewer$key = {
    readonly " $data"?: Test_viewer$data;
    readonly " $fragmentRefs": FragmentRefs<"Test_viewer">;
};



const node: ReaderFragment = {
  "argumentDefinitions": [],
  "kind": "Fragment",
  "metadata": {
    "refetch": {
      "connection": null,
      "fragmentPathInResult": [
        "viewer"
      ],
      "operation": require('./ViewerQuery.graphql.ts')
    }
  },
  "name": "Test_viewer",
  "selections": [
    {
      "alias": null,
      "args": null,
      "kind": "ScalarField",
      "name": "name",
      "storageKey": null
    }
  ],
  "type": "Viewer",
  "abstractKey": null
};
(node as any).hash = '16bd6cce9f7608bf2fbc7e4080dc4e63';
export default node;
