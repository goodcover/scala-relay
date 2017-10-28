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

import type {FormatModule} from './writeRelayScalaFile';

const formatGeneratedModule: FormatModule = ({
  moduleName,
  documentType,
  docText,
  concreteText,
  flowText,
  hash,
  devTextGenerator,
  relayRuntimeModule,
  packageName,
}) => {
  const objectName = documentType === 'ConcreteBatch' ? 'batch' : 'fragment';
  const docTextComment = docText ? '\n/*\n' + docText.trim() + '\n*/\n' : '';
  const hashText = hash ? `\n * ${hash}` : '';
  const devOnlyText = devTextGenerator ? devTextGenerator(objectName) : '';
  return `/**
 * scala ${hashText}
 * Generated, DON'T MANUALLY EDIT.
 */
package ${packageName};
/*::
import type {${documentType}} from '${relayRuntimeModule}';

objName:      ${objectName}
docType:      ${documentType}
concreteText: ${concreteText};
*/

import _root_.scala.scalajs.js

${flowText || ''}

${docTextComment}
object ${moduleName}  {
  val ast: _root_.scala.scalajs.js.Any = _root_.scala.scalajs.js.eval("""${concreteText}""")
  val query: String = """${devOnlyText}"""
}

`;
};

module.exports = formatGeneratedModule;