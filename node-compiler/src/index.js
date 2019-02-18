/**
 * @flow
 */


const find  = require("./FindGraphQLTags").find;
const formatGeneratedModule  = require("./codegen/formatScalaModule");
const typeGen  = require("./core/ScalaGenDirect");
// import { formatterFactory } from "./formatGeneratedModule";
// import { loadCompilerOptions } from "./loadCompilerOptions";
// import * as TypeScriptGenerator from "./TypeScriptGenerator";

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