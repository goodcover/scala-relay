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
 * relay-compiler-language-scalajs: ${hashText}
 * GENERATED, DON'T MANUALLY EDIT.
 * objName:      ${objectName}
 * docType:      ${documentType}
 */
package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

${docTextComment}

${typeText}
  lazy val query: _root_.relay.gql.${documentType} = _root_.scala.scalajs.js.eval("""${concreteText}""").asInstanceOf[_root_.relay.gql.${documentType}]
  lazy val sourceHash: String = "${sourceHash}"
}


`;
};

module.exports = formatGeneratedModule;