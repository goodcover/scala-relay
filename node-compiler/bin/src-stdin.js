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

class SingleGraphQLFileWriter {

  constructor(snippets) {
    this._documents = new Map();
    this._snippets = snippets;
  }

  // Short-term: we don't do subscriptions/delta updates, instead always use all definitions
  documents() {
    return ImmutableMap(this._documents);
  }

  // ignore files, it is using the captured snippets
  parseFiles(files) {
    let documents = ImmutableMap();
    this._snippets.forEach(template => {
      const ast = GraphQL.parse(template);

      invariant(
        ast.definitions.length,
        'RelayFileIRParser: Expected GraphQL text to contain at least one ' +
        'definition (fragment, mutation, query, subscription), got `%s`.',
        template
      );

      let doc =  {
        kind: 'Document',
        definitions: ast.definitions,
      };
      documents = documents.set(ast.definitions[0].name.value, doc);
      this._documents.set(ast.definitions[0].name.value, doc);
    });

    return documents;
  }
}


function getNewParser(templates) {
  return (baseDir) => {
    return new SingleGraphQLFileWriter(templates); 
  }
}

function getFileFilter(baseDir) {
  return (filename) => {
    return true;
  };
}

function fauxWriter() {

  return (onlyValidate, schema, documents, baseDocuments) => {
    
    return {
      writeAll: function() {
        const baseSchema = schema; 

        const config = {
          buildCommand: Utils.SCRIPT_NAME,
          compilerTransforms: {
            codegenTransforms,
            fragmentTransforms,
            printTransforms,
            queryTransforms,
          },
          schemaTransforms,
        };

        const transformedSchema = ASTConvert.transformASTSchema(
          baseSchema,
          config.schemaTransforms
        );

        const extendedSchema = ASTConvert.extendASTSchema(
          transformedSchema,
          baseDocuments.merge(documents).valueSeq().toArray()
        );

        const definitions = ASTConvert.convertASTDocumentsWithBase(
          extendedSchema,
          baseDocuments.valueSeq().toArray(),
          documents.valueSeq().toArray(),
          RelayValidator.LOCAL_RULES
        );

        const compilerContext = new RelayCompilerContext(extendedSchema);
        const compiler = new RelayCompiler(
          baseSchema,
          compilerContext,
          config.compilerTransforms
        );

        const nodes = compiler.addDefinitions(definitions);

        const transformedQueryContext = compiler.transformedQueryContext();
        const compiledDocumentMap = compiler.compile();
        const values = compiledDocumentMap.values().next().value
        
        console.log("---GQL:SCALA---");
        console.log(JSON.stringify(values));
        console.log("---EGQL:SCALA---")
        return ImmutableMap(); 
      }
    }
  };

}


function execute(srcDir, schema, writer, parser, fileFilter) {
  const parserConfigs = {
    default: {
      baseDir: srcDir,
      getFileFilter: fileFilter,
      getParser: parser,
      getSchema: () => Utils.getSchema(schema),
      watchmanExpression: Utils.WATCH_EXPRESSION,
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

function run(options) {
  var data = ""

  const schema = path.resolve(process.cwd(), options.schema);
  const out = path.resolve(process.cwd(), options.out);

  process.stdin.setEncoding('utf8');

  process.stdin.on('readable', () => {
    var chunk = process.stdin.read();
    if (chunk !== null) {
      data = data + chunk
    }
  });

  process.stdin.on('end', () => {
    var templated = template(data) 
    console.log(templated)
    templated = data;
    execute(out, schema, fauxWriter(), getNewParser([templated]), getFileFilter);
  });
}


function template(q) {
  return `graphql\`${q}\``;
}

const argv = yargs
  .usage(
    'Create Relay Files to Stdout\n\n' +
    '$0 --schema <path>')
  .options({
    'schema': {
      describe: 'Path to schema.graphql',
      demandOption: true,
      type: 'string',
    },
    'out': {
      describe: 'Path to dummy out',
      demandOption: true,
      type: 'string',
    }
  })
  .help()
  .argv;

run(argv);
