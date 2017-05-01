const FileParser = require('relay-compiler/lib/FileParser');
const GraphQL = require('graphql');

const fs = require('fs');
const invariant = require('invariant');
const path = require('path');

function parseFile(file) {
  const text = fs.readFileSync(file, 'utf8');
  const moduleName = path.basename(file, '.scala');

  invariant(
    text.indexOf('@gql') >= 0,
    'RelayFileIRParser: Files should be filtered before passed to the ' +
    'parser, got unfiltered file `%s`.',
    file
  )

  var regex = /@gql\("""([\s\S]*?)"""\)/g;

  const astDefinitions = [];
//   FindGraphQLTags.memoizedFind(text, moduleName).forEach(({tag, template}) => {
//     if (!(tag === 'graphql' || tag === 'graphql.experimental')) {
//       throw new Error(
//         'Invalid tag ' + tag + ' in module ' + moduleName + '. Expected `graphql` ' +
//         ' (common case) or `graphql.experimental` (if using experimental ' +
//         'directives).'
//       );
//     }
//     if (
//       tag !== 'graphql.experimental' &&
//       /@argument(Definition)?s\b/.test(template)
//     ) {
//       throw new Error(
//         'Unexpected use of fragment variables: @arguments and ' +
//         '@argumentDefinitions are only supported in ' +
//         'graphql.experimental literals. Source: ' + template
//       );
//     }
//     const ast = GraphQL.parse(template);
//     invariant(
//       ast.definitions.length,
//       'RelayFileIRParser: Expected GraphQL text to contain at least one ' +
//       'definition (fragment, mutation, query, subscription), got `%s`.',
//       template
//     );

//     astDefinitions.push(...ast.definitions);
//   });


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
  return new FileParser({
    baseDir,
    parse: parseFile,
  });
}

function getFileFilter(baseDir) {
  return (filename) => {
    const text = fs.readFileSync(path.join(baseDir, filename), 'utf8');
    return text.indexOf('@gql') >= 0;
  };
}

module.exports = {
  getParser,
  getFileFilter,
};
