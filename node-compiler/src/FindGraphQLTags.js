/**
 * @flow
 */

const path = require('path');

const GraphQLTag = require('relay-compiler').GraphQLTag;

const GraphQL = require('graphql');

const invariant = require('invariant');


/*
  Parse the file scala style but just use regex =(
*/
function parseFile(text, file): [] {
  var matches;

  if (path.extname(file) === '.scala') {

    invariant(
      text.indexOf('@graphql') >= 0 ||
      text.indexOf("graphqlGen") >= 0,
      'RelayFileIRParser: Files should be filtered before passed to the ' +
      'parser.',
    )

    var regex = /@graphql\([\s]*"""([\s\S]*?)"""\)/g;

    const astDefinitions = [];

    while (matches = regex.exec(text)) {
      const template = matches[1];

      const keyName = GraphQL.parse(template).definitions.map(f => f.name.value)[0].split("_")[1];
      const stringHit = matches.input.substring(0, matches.index - 1);
      const lineNo = stringHit.split("\n").length;
      const column = matches.index - stringHit.lastIndexOf("\n");


      astDefinitions.push({
        keyName,
        template,
        sourceLocationOffset: {
          line: lineNo,
          column: column
        }
      });
    }

    const regex2 = /graphqlGen\([\s]*"""([\s\S]*?)"""\)/g;

    while (matches = regex2.exec(text)) {
      const template = matches[1];

      const keyName = GraphQL.parse(template).definitions.map(f => f.name.value)[0].split("_")[1];
      const stringHit = matches.input.substring(0, matches.index - 1);
      const lineNo = stringHit.split("\n").length;
      const column = matches.index - stringHit.lastIndexOf("\n");


      astDefinitions.push({
        keyName,
        template,
        sourceLocationOffset: {
          line: lineNo,
          column: column
        }
      });
    }

    return astDefinitions;

  } else if (path.extname(file) === '.gql') {

    const astDefinitions = [];

    const keyName = GraphQL.parse(text).definitions.map(f => f.name.value)[0].split("_")[1];
    const template = text;

    astDefinitions.push({
      keyName,
      template,
      sourceLocationOffset: {
        line: 1,
        column: 1
      },
    });

    return astDefinitions;
  } else {
    invariant(
      false,
      'RelayFileIRParser: Files should be filtered before passed to the ' +
      'parser, got unfiltered file `%s`. Should either have a .gql extension and be a ' +
      'single query/fragment/mutation or be embedded in a .scala file as an annotation @graphql("""...""") ' +
      'or as a function, compiler.graphqlGen("""...""")',
      file
    )
  }
}

function find(text, filePath): Array<GraphQLTag> {
  const ast = parseFile(text, filePath);
  return ast;
}

module.exports = {
  find,
};
