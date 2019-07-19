/**
 * @flow
 */


const find  = require("./FindGraphQLTags").find;
const formatGeneratedModule  = require("./codegen/formatScalaModule");
const typeGen  = require("./core/ScalaGenDirect");

function plugin() {
    return {
        inputExtensions: ["scala", "gql"],
        outputExtension: "scala",
        findGraphQLTags: find,
        formatModule: formatGeneratedModule,
        typeGenerator: typeGen
    };
}

module.exports = plugin