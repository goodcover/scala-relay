/**
 * Copyright (c) 2013-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @providesModule formatGeneratedModule
 * @flow
 * @format
 */

'use strict';

const formatGeneratedModule: FormatModule = ({
  moduleName,
  documentType,
  docText,
  concreteText,
  typeText,
  hash,
  kind,
  sourceHash,
  devOnlyAssignments,
}) => {
  const objectName = documentType === 'ConcreteBatch' ? 'batch' : 'fragment';
  const docTextComment = docText ? '\n/*\n' + docText.trim() + '\n*/\n' : '';
  const hashText = hash ? `\n * ${hash}` : '';
  return `/**
 * scala-relay-compiler: ${hashText}
 * GENERATED, DON'T MANUALLY EDIT.
 * objName:      ${objectName}
 * docType:      ${documentType}
 */

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

${typeText || ''}

${docTextComment}


`;
};

module.exports = formatGeneratedModule;