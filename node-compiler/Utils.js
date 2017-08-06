const fs = require('fs');
const path = require('path');
const yargs = require('yargs');
const invariant = require('invariant');

const ASTConvert = require('relay-compiler/lib/ASTConvert');

const formatGeneratedModule = require('relay-compiler/lib/formatGeneratedModule');
const RelayCodegenRunner = require('relay-compiler').Runner;
const RelayConsoleReporter = require('relay-compiler').ConsoleReporter;
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
  schemaExtensions,
} = RelayIRTransforms;

const SCRIPT_NAME = 'relay-compiler';

const WATCH_EXPRESSION = [
  'allof',
  ['type', 'f'],
  ['suffix', 'scala'],
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
      formatModule: formatGeneratedModule,
      compilerTransforms: {
        codegenTransforms,
        fragmentTransforms,
        printTransforms,
        queryTransforms,
      },
      baseDir,
      schemaExtensions,
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
      isGeneratedFile: (filePath) => true
    },
  };
  const reporter = new RelayConsoleReporter({verbose: true});

  const codegenRunner = new RelayCodegenRunner({
    reporter,
    parserConfigs,
    writerConfigs,
    onlyValidate: false
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