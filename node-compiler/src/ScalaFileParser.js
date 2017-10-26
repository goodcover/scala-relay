const RelayJSModuleParser = require('relay-compiler/lib/RelayJSModuleParser');
const GraphQL = require('graphql');

const fs = require('fs');
const invariant = require('invariant');
const path = require('path');

const {ASTCache} = require('relay-compiler/lib/GraphQLCompilerPublic');

/*
  Parse the file scala style but just use regex =(
*/
function parseFile(baseDir, file) {
  const text = fs.readFileSync(path.join(baseDir, file.relPath), 'utf8');

  invariant(
    text.indexOf('@gql') >= 0,
    'RelayFileIRParser: Files should be filtered before passed to the ' +
    'parser, got unfiltered file `%s`.',
    file
  )

  var regex = /@gql\([\s\S]*?""?"?([\s\S]*?)""?"?[\s\S]*\)/g;

  const astDefinitions = [];


  while (matches = regex.exec(text)) {
      const template = matches[1];

      const ast = GraphQL.parse(template);
      invariant(
        ast.definitions.length,
        'RelayFileIRParser: Expected GraphQL text to contain at least one ' +
        'definition (fragment, mutation, query, subscription), got `%s`.',
        template
      );

      astDefinitions.push(...ast.definitions);  
  }
  return {
    kind: 'Document',
    definitions: astDefinitions,
  };
}

function getParser(baseDir) {
  return new ASTCache({
    baseDir,
    parse: parseFile,
  });
}

function getFileFilter(baseDir) {
  return (file) => {
    const text = fs.readFileSync(path.join(baseDir, file.relPath), 'utf8');
    return text.indexOf('@gql') >= 0;
  };
}

function getFilepathsFromGlob(
  baseDir,
  options
) {
  const {extensions, include, exclude} = options;
  const patterns = include.map(inc => `${inc}/*.+(${extensions.join('|')})`);

  const glob = require('fast-glob');
  return glob.sync(patterns, {
    cwd: baseDir,
    bashNative: [],
    onlyFiles: true,
    ignore: exclude,
  });
}

module.exports = {
  getParser,
  getFileFilter,
  getFilepathsFromGlob
};
