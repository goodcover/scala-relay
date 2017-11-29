// @flow

require('babel-polyfill');

const {
  CodegenRunner,
  ConsoleReporter,
  WatchmanClient,
} = require('relay-compiler/lib/GraphQLCompilerPublic');

const RelayJSModuleParser = require('relay-compiler/lib/RelayJSModuleParser');
const ScalaFileWriter = require('./codegen/ScalaFileWriter');
const {FileWriter} = require('relay-compiler');
const RelayIRTransforms = require('relay-compiler/lib/RelayIRTransforms');

const formatModule  = require('./codegen/formatScalaModule');
const fs = require('fs');
const path = require('path');
const yargs = require('yargs');

const {
  buildASTSchema,
  buildClientSchema,
  parse,
  printSchema,
} = require('graphql');

const {
  codegenTransforms,
  fragmentTransforms,
  printTransforms,
  queryTransforms,
  schemaExtensions,
} = RelayIRTransforms;

const SCRIPT_NAME = 'relay-compiler';

const SJS = require('./transforms/SJSTransform');

const verbose = false;

const WATCH_EXPRESSION = [
  'allof',
  ['type', 'f'],
  ['suffix', 'scala'],
  ['not', ['match', '**/__mocks__/**', 'wholename']],
  ['not', ['match', '**/__tests__/**', 'wholename']],
  ['not', ['match', '**/__generated__/**', 'wholename']],
];

// Inject
printTransforms.unshift(SJS.transformRemoveSjs);

function getSchema(schemaPath: string) {
  try {
    let source = fs.readFileSync(schemaPath, 'utf8');
    source = `
  directive @include(if: Boolean) on FRAGMENT | FIELD
  directive @skip(if: Boolean) on FRAGMENT | FIELD
  directive @sjs(with: Boolean!) on FRAGMENT | FIELD
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

function getScalaFileWriter(baseDir: string, outputDir: string) {
  // $FlowFixMe
  return (onlyValidate, schema, documents, baseDocuments) =>
  new ScalaFileWriter({ 
    config: {
      formatModule,
      compilerTransforms: {
        codegenTransforms,
        fragmentTransforms,
        printTransforms,
        queryTransforms,
      },
      outputDir,
      baseDir,
      schemaExtensions,
    },
    onlyValidate,
    schema,
    baseDocuments,
    documents,
  });
}



// $FlowFixMe
function compileAll(srcDir: string, schemaPath: string, writer, parser, fileFilter, getFilepathsFromGlob) {
  const files = getFilepathsFromGlob(srcDir, {include: ["**"], extensions: ["scala"]});

  const parserConfigs = {
    default: {
      baseDir: srcDir,
      getFileFilter: fileFilter,
      getParser: parser,
      getSchema: () => getSchema(schemaPath),
      watchmanExpression: null,
      filepaths: files,
    },
  };
  const writerConfigs = {
    default: {
      getWriter: writer,
      parser: 'default',
      isGeneratedFile: (filePath) => true
    },
  };
  const reporter = new ConsoleReporter({verbose});

  // $FlowFixMe
  const codegenRunner = new CodegenRunner({
    reporter,
    parserConfigs,
    writerConfigs,
    onlyValidate: false
  });

  // $FlowFixMe
  codegenRunner.compileAll().then(
    () => process.exit(0),
    error => {
      console.error(String(error.stack || error));
      process.exit(1);
    });
}



module.exports = {
  getSchema,
  getScalaFileWriter,
  WATCH_EXPRESSION,
  SCRIPT_NAME,
  compileAll
}