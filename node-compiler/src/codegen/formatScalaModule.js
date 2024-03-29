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

var Terser = require("terser");

const formatGeneratedModule: FormatModule = ({
  moduleName,
  documentType,
  docText,
  concreteText,
  definition,
  typeText,
  hash,
  kind,
  sourceHash,
  devOnlyAssignments,
}) => {
  const objectName = documentType === 'ConcreteBatch' ? 'batch' : 'fragment';
  const docTextComment = docText ? '\n/*\n' + docText.trim() + '\n*/\n' : '';
  const hashText = hash ? `\n * ${hash}` : '';

  const result = Terser.minify(`(${concreteText})`, {compress: false});
  const code = result.code || concreteText;
  const d = definition.metadata
  const resultOfRefetch = d && d.refetch && d.refetch.operation
  const refetchTempl = resultOfRefetch ? `// Refetchable query
    defn.metadata.refetch.operation = _root_.relay.generated.${resultOfRefetch}.query` : ``

  const queryTypeParams =
        documentType === 'ReaderFragment' ? '[Ctor, Out]' :
        documentType === 'ReaderInlineDataFragment' ? '[Ctor, Out]' :
        '';

  return `/**
 * relay-compiler-language-scalajs: ${hashText}
 * GENERATED, DON'T MANUALLY EDIT.
 * objName:      ${objectName}
 * docType:      ${documentType}
 * module:       ${moduleName}
 */
package relay.generated

import _root_.scala.scalajs.js
import _root_.scala.scalajs.js.|

${docTextComment}

${typeText}
  // Used to differentiate between normal and inline query types.
  type Query = _root_.relay.gql.${documentType}${queryTypeParams}

  lazy val query: Query = {
    val defn = _root_.scala.scalajs.js.Function("""return ${code}""").call(null)
    ${refetchTempl}
    defn.asInstanceOf[Query]
  }
  lazy val sourceHash: String = "${sourceHash}"
}


`;
};

module.exports = formatGeneratedModule;
