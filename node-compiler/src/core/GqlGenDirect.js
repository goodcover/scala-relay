/**
 * Copyright (c) 2013-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @providesModule RelayFlowGenerator
 * @flow
 * @format
 */

'use strict';

// const t = require('babel-types');

const Printer = require('relay-compiler').Printer;
const Profiler = require('relay-compiler').Profiler;
// console.log(Profiler);


const RelayRelayDirectiveTransform = require('relay-compiler/lib/transforms/RelayDirectiveTransform');
const RelayMaskTransform = require('relay-compiler/lib/transforms/MaskTransform');
const RelayMatchTransform = require('relay-compiler/lib/transforms/MatchTransform');
// TODO: Look at adding these
const FlattenTransform = require("relay-compiler/lib/transforms/FlattenTransform");
const RequiredFieldTransform = require('relay-compiler/lib/transforms/RequiredFieldTransform');
const RelayRefetchableFragmentTransform = require('relay-compiler/lib/transforms/RefetchableFragmentTransform');

const SJSTransform = require("../transforms/SJSTransform");


import type {Schema} from "relay-compiler/core/Schema";

import type {Fragment, IRTransform, Root} from 'relay-compiler/lib/RelayCompilerPublic';

function generate(
  schema: Schema,
  node: Root | Fragment,
): string {
  return Printer.print(schema, node)
}

const FLOW_TRANSFORMS: Array<IRTransform> = [
  RelayRelayDirectiveTransform.transform,
  RelayMaskTransform.transform,
  // ConnectionFieldTransform.transform,
  RelayMatchTransform.transform,
  RequiredFieldTransform.transform,
  FlattenTransform.transformWithOptions({}),
  RelayRefetchableFragmentTransform.transform
];

const schemaExtensions: Array<string> = [
  SJSTransform.SCHEMA_EXTENSION
]

module.exports = {
  generate: Profiler.instrument(generate, 'RelayScala2GqlGenerator.generate'),
  transforms: FLOW_TRANSFORMS,
  schemaExtensions,
};
