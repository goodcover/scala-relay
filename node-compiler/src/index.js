/**
 * @flow
 */

import type PluginInterface from "relay-compiler/language/RelayLanguagePluginInterface";

const find = require("./FindGraphQLTags").find;
const formatScalaModule = require("./codegen/formatScalaModule");
const formatGqlModule = require("./codegen/formatGqlModule");
const scalaTypeGen = require("./core/ScalaGenDirect");
const gqlTypeGen = require("./core/GqlGenDirect");
const fileFilter = require("./core/SjsFileFilter").fileFilter;

type Config = {
  mode?: "scalajs" | "gql"
}

const commonDetails = {
  inputExtensions: ["scala", "gql"],
  findGraphQLTags: find,
  schemaExtensions: scalaTypeGen.schemaExtensions,
  getFileFilter: fileFilter,
}

const scalajsDetails = {
  outputExtension: "scala",
  formatModule: formatScalaModule,
  typeGenerator: scalaTypeGen,
}

const gqlDetails = {
  // Unfortunately we cannot use the same plugin to generate two different types of files since there may only be one
  // output extension.
  outputExtension: "gql",
  formatModule: formatGqlModule,
  typeGenerator: gqlTypeGen,
}

// Look at the interface here,
// https://github.com/facebook/relay/blob/v11.0.2/packages/relay-compiler/language/RelayLanguagePluginInterface.js
function plugin(config?: Config): PluginInterface {
  return {
    ...commonDetails,
    ...(config?.mode === "gql" ? gqlDetails : scalajsDetails)
  };
}

module.exports = plugin
