#!/usr/bin/env node

const ASTConvert = require('relay-compiler/lib/ASTConvert');

const RelayCodegenRunner = require('relay-compiler').Runner;
const RelayFileIRParser = require('relay-compiler').FileIRParser;
const RelayFileWriter = require('relay-compiler').FileWriter;
const RelayIRTransforms = require('relay-compiler').IRTransforms;
const RelayValidator = require('relay-compiler/lib/RelayValidator');
const RelayCompiler = require('relay-compiler/lib/RelayCompiler');
const RelayCompilerContext = require('relay-compiler/lib/RelayCompilerContext');
const FileParser = require('relay-compiler/lib/FileParser');

const rfp = require("relay-compiler/lib/RelayFlowParser");
const GraphQL = require('graphql');

const fs = require('fs');
const path = require('path');
const yargs = require('yargs');
const invariant = require('invariant');

const { Map: ImmutableMap } = require('immutable');

const { buildASTSchema, parse } = require('graphql');
const {
  codegenTransforms,
  fragmentTransforms,
  printTransforms,
  queryTransforms,
  schemaTransforms,
} = RelayIRTransforms;


const ScalaFileParser = require('../ScalaFileParser');
const ConsoleUtils = require('../ConsoleUtils');
const Utils = require('../Utils');


function run(options) {
  const schema = path.resolve(process.cwd(), options.schema);
  const src = path.resolve(process.cwd(), options.src);
  const out = path.resolve(process.cwd(), options.out);

  Utils.compileAll(src, 
    schema,
    Utils.getRelayFileWriter(src, out), 
    ScalaFileParser.getParser, 
    ScalaFileParser.getFileFilter);
}

const argv = yargs
  .usage(
    'Create Relay generated files\n\n' +
    '$0 --schema <path> --src <path> [--watch]')
  .options({
    'schema': {
      describe: 'Path to schema.graphql',
      demandOption: true,
      type: 'string',
    },
    'src': {
      describe: 'Root directory of scala application code',
      demandOption: true,
      type: 'string',
    },
    'out': {
      describe: 'Output of the runtime relay fragments',
      demandOption: true,
      type: 'string',
    }
  })
  .help()
  .argv;

run(argv);