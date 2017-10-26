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

      invariant(ast.definitions.length, 'RelayFileIRParser: Expected GraphQL text to contain at least one ' + 'definition (fragment, mutation, query, subscription), got `%s`.', template);

      let doc = {
        kind: 'Document',
        definitions: ast.definitions
      };
      documents = documents.set(ast.definitions[0].name.value, doc);
      this._documents.set(ast.definitions[0].name.value, doc);
    });

    return documents;
  }
}

function getParser(templates) {
  return baseDir => {
    return new SnippetFakeFileParser(templates);
  };
}

function getFileFilter(baseDir) {
  return filename => {
    return true;
  };
}

function getConsoleWriter() {

  return (onlyValidate, schema, documents, baseDocuments) => {

    return {
      writeAll: function () {
        const baseSchema = schema;

        const config = {
          buildCommand: SCRIPT_NAME,
          compilerTransforms: {
            codegenTransforms,
            fragmentTransforms,
            printTransforms,
            queryTransforms
          },
          schemaTransforms
        };

        const transformedSchema = ASTConvert.transformASTSchema(baseSchema, config.schemaTransforms);

        const extendedSchema = ASTConvert.extendASTSchema(transformedSchema, baseDocuments.merge(documents).valueSeq().toArray());

        const definitions = ASTConvert.convertASTDocumentsWithBase(extendedSchema, baseDocuments.valueSeq().toArray(), documents.valueSeq().toArray(), RelayValidator.LOCAL_RULES);

        const compilerContext = new RelayCompilerContext(extendedSchema);
        const compiler = new RelayCompiler(baseSchema, compilerContext, config.compilerTransforms);

        const nodes = compiler.addDefinitions(definitions);

        const transformedQueryContext = compiler.transformedQueryContext();
        const compiledDocumentMap = compiler.compile();
        console.log(JSON.stringify(compiledDocumentMap, null, 2));
        return ImmutableMap();
      }
    };
  };
}

module.exports = {
  getParser,
  getFileFilter,
  getConsoleWriter
};