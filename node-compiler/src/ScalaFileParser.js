const RelayJSModuleParser = require('relay-compiler/lib/RelayJSModuleParser');
const GraphQL = require('graphql');

const fs = require('fs');
const invariant = require('invariant');
const path = require('path');

const {ASTCache} = require('relay-compiler/lib/GraphQLCompilerPublic');

import type {File, FileFilter} from 'graphql-compiler';
import type {DocumentNode} from 'graphql';

/*
  Parse the file scala style but just use regex =(
*/
function parseFile(baseDir, file) {
  const text = fs.readFileSync(path.join(baseDir, file.relPath), 'utf8');
  var matches;

  invariant(
    text.indexOf('@gql') >= 0,
    'RelayFileIRParser: Files should be filtered before passed to the ' +
    'parser, got unfiltered file `%s`.',
    file
  )

  var regex = /@gql\("""([\s\S]*?)"""\)/g;

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

function getFileFilter(baseDir): FileFilter {
  return (file: File) => {
    const text = fs.readFileSync(path.join(baseDir, file.relPath), 'utf8');
    return text.indexOf('@gql') >= 0;
  };
}

function getFilepathsFromGlob(
  baseDir,
  {extensions, include, exclude}
) {
  const patterns = include.map(inc => `${inc}/*.+(${extensions.join('|')})`);

  const glob = require('fast-glob');
  return glob.sync(patterns, {
    cwd: baseDir,
    bashNative: [],
    onlyFiles: true,
    ignore: exclude || [],
  });
}

module.exports = {
  getParser,
  getFileFilter,
  getFilepathsFromGlob
};
