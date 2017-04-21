const BabelPluginRelay = require('babel-plugin-relay');
const babel = require('babel-core');

const ASTConvert = require('relay-compiler/lib/ASTConvert');

const RelayCodegenRunner = require('relay-compiler').Runner;
const RelayFileIRParser = require('relay-compiler').FileIRParser;
const RelayFileWriter = require('relay-compiler').FileWriter;
const RelayIRTransforms = require('relay-compiler').IRTransforms;
const RelayValidator = require('relay-compiler/lib/RelayValidator');
const RelayCompiler = require('relay-compiler/lib/RelayCompiler');
const RelayCompilerContext = require('relay-compiler/lib/RelayCompilerContext');
const FileParser = require('relay-compiler/lib/FileParser');

const rewire = require('rewire');
const rfp = require("relay-compiler/lib/RelayFlowParser");
const GraphQL = require('graphql');

const fs = require('fs');
const path = require('path');
const yargs = require('yargs');
const invariant = require('invariant');

const {Map: ImmutableMap} = require('immutable');

const {buildASTSchema, parse} = require('graphql');
const {
  codegenTransforms,
  fragmentTransforms,
  printTransforms,
  queryTransforms,
  schemaTransforms,
} = RelayIRTransforms;

const SCRIPT_NAME = 'relay-compiler';
const SCHEMA_PATH = path.resolve('./schema.graphql');

const WATCH_EXPRESSION = [
  'allof',
  ['type', 'f'],
  ['suffix', 'js'],
  // ['suffix', 'scala'],
  ['not', ['match', '**/__mocks__/**', 'wholename']],
  ['not', ['match', '**/__tests__/**', 'wholename']],
  ['not', ['match', '**/__generated__/**', 'wholename']],
];

const q = `fragment DictionaryComponent_word on Word {
    id
    definition {
      ...DictionaryComponent_definition
    }
  }`;

const text = `graphql\`${q}\``;



function foo(srcDir, writer, parser, fileFilter) {
  const parserConfigs = {
    default: {
      baseDir: srcDir,
      getFileFilter: fileFilter,
      getParser: parser,
      getSchema: () => getSchema(SCHEMA_PATH),
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

function getRelayFileWriter(baseDir) {
  // return (onlyValidate, schema, documents, baseDocuments) => {
  //   console.log(onlyValidate, schema, documents, baseDocuments);
  // };
  return (onlyValidate, schema, documents, baseDocuments) => new RelayFileWriter({
    config: {
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

// console.log(babel.transform(text, {
//     plugins: [
//       BabelPluginRelay,
//     ],
//     compact: false,
//     parserOpts: {plugins: ['jsx']},
// }).code);

function newWriter() {

  return (onlyValidate, schema, documents, baseDocuments) => {
    
    return {
      writeAll: function() {
        const baseSchema = schema; 

        const config = {
          buildCommand: SCRIPT_NAME,
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
        console.log(JSON.stringify(compiledDocumentMap, null, 2));
        return ImmutableMap(); 
      }
    }
  };

}








class SnippetFakeFileParser {

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
    return new SnippetFakeFileParser(templates); 
  }
}

function getFileFilter(baseDir) {
  return (filename) => {
    return true;
  };
}



// const src = path.resolve(process.cwd())
// foo(src, newWriter(), getNewParser([q]), getFileFilter);

var data = ""

// process.stdin.setEncoding('utf8');

// process.stdin.on('readable', () => {
//   var chunk = process.stdin.read();
//   if (chunk !== null) {
//     data = data + chunk
//   }
// });

// process.stdin.on('end', () => {
//   // foo(src, newWriter(), getNewParser([data]), getFileFilter);
// });

const src = path.resolve(process.cwd(), "src")
const fr = require('./file-runner.js');

foo(src, getRelayFileWriter(src), fr.getParser, fr.getFileFilter)