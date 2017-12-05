/**
 * Copyright (c) 2013-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @providesModule writeRelayGeneratedFile
 * @flow
 * @format
 */

'use strict';

const crypto = require('crypto');
const invariant = require('invariant');
// TODO T21875029 ../../relay-runtime/util/prettyStringify
const prettyStringify = require('relay-runtime/lib/prettyStringify');

import type {CodegenDirectory} from 'relay-compiler/lib/GraphQLCompilerPublic';
// TODO T21875029 ../../relay-runtime/util/RelayConcreteNode
import type {GeneratedNode} from 'relay-compiler/lib/RelayConcreteNode';

/**
 * Generate a module for the given document name/text.
 */
export type FormatModule = ({|
  moduleName: string,
  documentType: 'ConcreteBatch' | 'ConcreteFragment',
  docText: ?string,
  concreteText: string,
  topClasses: ?string,
  hash: ?string,
  devTextGenerator: (objectName: string) => string,
  relayRuntimeModule: string,
  packageName: string,
  supportingClasses: ?string,
  implicits: ?string,
  objectParent: ?string,
|}) => string;

async function writeRelayScalaFile(
  codegenDir: CodegenDirectory,
  generatedNode: GeneratedNode,
  formatModule: FormatModule,
  topClasses: ?string,
  persistQuery: ?(text: string) => Promise<string>,
  platform: ?string,
  relayRuntimeModule: string,
  defaultPackage: ?string,
  supportingClasses: ?string,
  implicits: ?string,
  objectParent: ?string,
): Promise<?GeneratedNode> {
  const moduleName = generatedNode.name;
  const platformName = platform ? moduleName + '.' + platform : moduleName;
  const filename = platformName + '.scala';
  const flowTypeName =
    generatedNode.kind === 'Batch' ? 'ConcreteBatch' : 'ConcreteFragment';
  const devOnlyProperties = {};

  const packageName = defaultPackage ? defaultPackage: "relay.generated";

  let text = null;
  let hash = null;
  if (generatedNode.kind === 'Batch') {
    text = generatedNode.text;
    invariant(
      text,
      'codegen-runner: Expected query to have text before persisting.',
    );
    const oldContent = codegenDir.read(filename);
    // Hash the concrete node including the query text.
    const hasher = crypto.createHash('md5');
    hasher.update('cache-breaker-2');
    hasher.update(JSON.stringify(generatedNode));
    if (topClasses) {
      hasher.update(topClasses);
    }
    if (supportingClasses) {
      hasher.update(supportingClasses);
    }
    if (implicits) {
      hasher.update(implicits);
    }
    if (objectParent) {
      hasher.update(objectParent);
    }
    if (persistQuery) {
      hasher.update('persisted');
    }
    hash = hasher.digest('hex');
    if (hash === extractHash(oldContent)) {
      codegenDir.markUnchanged(filename);
      return null;
    }
    if (codegenDir.onlyValidate) {
      codegenDir.markUpdated(filename);
      return null;
    }
    if (persistQuery) {
      generatedNode = {
        ...generatedNode,
        text: null,
        id: await persistQuery(text),
      };

      devOnlyProperties.text = text;
    }
  }

  const moduleText = formatModule({
    moduleName,
    documentType: flowTypeName,
    docText: text,
    topClasses,
    hash: hash ? `@relayHash ${hash}` : null,
    concreteText: prettyStringify(generatedNode),
    devTextGenerator: makeDevTextGenerator(devOnlyProperties),
    relayRuntimeModule,
    packageName,
    supportingClasses,
    implicits,
    objectParent
  });

  codegenDir.writeFile(filename, moduleText);
  return generatedNode;
}

function makeDevTextGenerator(devOnlyProperties: Object) {
  return objectName => {
    const assignments = Object.keys(devOnlyProperties).map(key => {
      const value = devOnlyProperties[key];
      const stringifiedValue =
        value === undefined ? 'undefined' : JSON.stringify(value);

      return `  ${objectName}['${key}'] = ${stringifiedValue};`;
    });

    if (!assignments.length) {
      return '';
    }

    return `

if (__DEV__) {
${assignments.join('\n')}
}
`;
  };
}

function extractHash(text: ?string): ?string {
  if (!text) {
    return null;
  }
  if (/<<<<<|>>>>>/.test(text)) {
    // looks like a merge conflict
    return null;
  }
  const match = text.match(/@relayHash (\w{32})\b/m);
  return match && match[1];
}

module.exports = writeRelayScalaFile;