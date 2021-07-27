/**
 * @flow
 */


const find = require("./FindGraphQLTags").find;
const formatGeneratedModule = require("./codegen/formatScalaModule");
const typeGen = require("./core/ScalaGenDirect");
const fileFilter = require("./core/SjsFileFilter").fileFilter;

// Look at the interface here,
// https://github.com/facebook/relay/blob/master/packages/relay-compiler/language/RelayLanguagePluginInterface.js
function plugin() {
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
