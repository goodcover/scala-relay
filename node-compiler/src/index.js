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

const propSuffixRegex = /_.*/;

// TODO: In hindsight this was the wrong way to do this.
//  We should change the scalajs plugin so that it puts the gql as a comment in the generated Scala files for fragments
//  like it does for queries etc. Then we share the generated Scala files across projects and have the scalajs plugin
//  read this gql out of these files so that the downstream queries will pass validation but not emit duplicate code
//  for these shared fragments.
const gqlDetails = {
  // Unfortunately we cannot use the same plugin to generate two different types of files since there may only be one
  // output extension.
  outputExtension: "gql",
  formatModule: formatGqlModule,
  typeGenerator: gqlTypeGen,
  getModuleName: (operationName: string) => {
    // Remove the _<propName> suffix otherwise when we attempt to use the gql file relay-compiler thinks that the prop
    // name is part of the module name and then complains that the fragment does not have the correct name.
    return operationName.replace(propSuffixRegex, '');
  },
  isGeneratedFile: (filePath: string) => {
    // How are we supposed to know this? It fails if we don't return true for those in the generated directory.
    return filePath.endsWith(".gql")
  }
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
