/**
 * @flow
 */

import type PluginInterface from "relay-compiler/language/RelayLanguagePluginInterface";

const find = require("./FindGraphQLTags").find;
const formatGeneratedModule = require("./codegen/formatScalaModule");
const typeGen = require("./core/ScalaGenDirect");
const fileFilter = require("./core/SjsFileFilter").fileFilter;

// Look at the interface here,
// https://github.com/facebook/relay/blob/v11.0.2/packages/relay-compiler/language/RelayLanguagePluginInterface.js
function plugin(): PluginInterface {
  return {
    inputExtensions: ["scala", "gql"],
    outputExtension: "scala",
    findGraphQLTags: find,
    formatModule: formatGeneratedModule,
    typeGenerator: typeGen,
    schemaExtensions: typeGen.schemaExtensions,
    getFileFilter: fileFilter,
  };
}

module.exports = plugin
