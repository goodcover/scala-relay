#!/usr/bin/env node

require('@babel/polyfill');

const {
  CodegenRunner,
  ConsoleReporter,
} = require('relay-compiler/lib/RelayCompilerPublic');

const RelayJSModuleParser = require('relay-compiler/lib/RelaySourceModuleParser');
const RelayIRTransforms = require('relay-compiler/lib/RelayIRTransforms');

const GraphQL = require('graphql');

const fs = require('fs');
const path = require('path');
const yargs = require('yargs');
const invariant = require('invariant');

const { Map: ImmutableMap } = require('immutable');

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
  schemaTransforms,
} = RelayIRTransforms;


const ScalaFileParser = require('../lib/ScalaFileParser');
const Utils = require('../lib/Utils');


function run(options) {
  const schema = path.resolve(process.cwd(), options.schema);
  const out = path.resolve(process.cwd(), options.out);
  const extras = (options.extra || [""]);

  Utils.compileAll(options.src,
    extras,
    schema,
    Utils.getScalaFileWriter(options.src, out, options.useNulls),
    ScalaFileParser.getParser,
    ScalaFileParser.getFileFilter,
    ScalaFileParser.getFilepathsFromGlob,
    options.verbose);
}

const argv = yargs
  .usage(
    'Create Relay generated files\n\n' +
    '$0 --schema <path> --src <path> --out <path>')
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
    'extra': {
      describe: 'Extra directory gqls',
      demandOption: false,
      type: 'array',
    },
    'out': {
      describe: 'Output of the runtime relay fragments',
      demandOption: true,
      type: 'string',
    },
    'verbose': {
      describe: 'Output everything?',
      demandOption: false,
      type: 'boolean',
      default: false
    },
    'useNulls': {
      describe: 'For attributes that are optional use A | Null vs A',
      demandOption: false,
      type: 'boolean',
      default: false
    },
  })
  .help()
  .argv;

run(argv);