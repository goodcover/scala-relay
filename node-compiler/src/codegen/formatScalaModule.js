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
  topClasses,
  hash,
  devTextGenerator,
  relayRuntimeModule,
  packageName,
  supportingClasses,
  implicits,
  objectParent,
}) => {
  const objectName = documentType === 'ConcreteBatch' ? 'batch' : 'fragment';
  const docTextComment = docText ? '\n/*\n' + docText.trim() + '\n*/\n' : '';
  const hashText = hash ? `\n * ${hash}` : '';
  const devOnlyText = devTextGenerator ? devTextGenerator(objectName) : '';
  return `/**
 * scala-relay-compiler: ${hashText}
 * GENERATED, DON'T MANUALLY EDIT.
 * objName:      ${objectName}
 * docType:      ${documentType}
 */
package ${packageName}

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

${topClasses || ''}

${docTextComment}
object ${moduleName} extends ${objectParent || '_root_.relay.graphql.GenericGraphQLTaggedNode'} {

  ////////////////////////////////////
  ////// Supporting classes begin here
  ////////////////////////////////////
${supportingClasses || ''}

  ///////////////////////////
  ////// Implicits begin here
  ///////////////////////////
${implicits || ''}

  val query: _root_.relay.graphql.${documentType} = _root_.scala.scalajs.js.eval("""${concreteText}""").asInstanceOf[_root_.relay.graphql.${documentType}]
  val devText: String = """${devOnlyText}"""
}

`;
};

module.exports = formatGeneratedModule;