const fs = require('fs');
const path = require('path');
const yargs = require('yargs');
const invariant = require('invariant');

const ASTConvert = require('relay-compiler/lib/ASTConvert');

const RelayCodegenRunner = require('relay-compiler').Runner;
const RelayFileIRParser = require('relay-compiler').FileIRParser;
const RelayFileWriter = require('relay-compiler').FileWriter;
const RelayIRTransforms = require('relay-compiler').IRTransforms;
const RelayValidator = require('relay-compiler/lib/RelayValidator');
const RelayCompiler = require('relay-compiler/lib/RelayCompiler');
const RelayCompilerContext = require('relay-compiler/lib/RelayCompilerContext');
const FileParser = require('relay-compiler/lib/FileParser');

const { buildASTSchema, parse } = require('graphql');
const {
  codegenTransforms,
  fragmentTransforms,
  printTransforms,
  queryTransforms,
  schemaTransforms,
} = RelayIRTransforms;

const SCRIPT_NAME = 'relay-compiler';

const WATCH_EXPRESSION = [
  'allof',
  ['type', 'f'],
  ['suffix', 'js'],
  // ['suffix', 'scala'],
  ['not', ['match', '**/__mocks__/**', 'wholename']],
  ['not', ['match', '**/__tests__/**', 'wholename']],
  ['not', ['match', '**/__generated__/**', 'wholename']],
];

function getSchema(schemaPath) {
  try {
    let source = fs.readFileSync(schemaPath, 'utf8');
    source = `
  directive @include(if: Boolean) on FRAGMENT | FIELD
  directive @skip(if: Boolean) on FRAGMENT | FIELD
  directive @relay(pattern: Boolean, plural: Boolean) on FRAGMENT | FIELD
  ${source}
  `;
    return buildASTSchema(parse(source));
  } catch (error) {
    throw new Error(`
Error loading schema. Expected the schema to be a .graphql file using the
GraphQL schema definition language. Error detail:
${error.stack}
    `.trim());
  }
}

function getRelayFileWriter(baseDir, outputDir) {
  // return (onlyValidate, schema, documents, baseDocuments) => {
  //   console.log(onlyValidate, schema, documents, baseDocuments);
  // };
  return (onlyValidate, schema, documents, baseDocuments) => new RelayFileWriter({
    config: {
      outputDir,
      buildCommand: SCRIPT_NAME,
      compilerTransforms: {
        codegenTransforms,
        fragmentTransforms,
        printTransforms,
        queryTransforms,
      },
      baseDir,
      schemaTransforms,
    },
    onlyValidate,
    schema,
    baseDocuments,
    documents,
  });
}

function compileAll(srcDir, schemaPath, writer, parser, fileFilter) {
  const parserConfigs = {
    default: {
      baseDir: srcDir,
      getFileFilter: fileFilter,
      getParser: parser,
      getSchema: () => getSchema(schemaPath),
      watchmanExpression: WATCH_EXPRESSION,
    },
  };
  const writerConfigs = {
    default: {
      getWriter: writer,
      parser: 'default',
    },
  };
  const codegenRunner = new RelayCodegenRunner({
    parserConfigs,
    writerConfigs,
    onlyValidate: false,
    skipPersist: false,
  });

  codegenRunner.compileAll().then(
    () => process.exit(0),
    error => {
      console.error(String(error.stack || error));
      process.exit(1);
    });
}



module.exports = {
  getSchema,
  getRelayFileWriter,
  WATCH_EXPRESSION,
  SCRIPT_NAME,
  compileAll
}