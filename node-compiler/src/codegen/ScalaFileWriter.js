/**
 * Copyright (c) 2013-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @providesModule ScalaFileWriter
 * @flow
 * @format
 */

'use strict';

const compileRelayArtifacts = require('relay-compiler/lib/compileRelayArtifacts');
const RelayFlowGenerator = require('../core/ScalaGenDirect');
const RelayParser = require('relay-compiler/lib/RelayParser');
const RelayValidator = require('relay-compiler/lib/RelayValidator');

const invariant = require('invariant');
const path = require('path');
const writeRelayScalaFile = require('./writeRelayScalaFile');

const {generate} = require('relay-compiler/lib/RelayCodeGenerator');
const {
  ASTConvert,
  CodegenDirectory,
  CompilerContext,
  SchemaUtils,
} = require('graphql-compiler');
const {Map: ImmutableMap} = require('immutable');

import type {ScalarTypeMapping} from 'relay-compiler/lib/RelayFlowTypeTransformers';
import type {
  CompiledDocumentMap,
  CompilerTransforms,
  FileWriterInterface,
  Reporter,
} from 'relay-compiler/lib/RelayCompilerPublic';
import type {FormatModule} from './writeRelayScalaFile';
// TODO T21875029 ../../relay-runtime/util/RelayConcreteNode
import type {GeneratedNode} from 'graphql-compiler';
import type {DocumentNode, GraphQLSchema, ValidationContext} from 'graphql';

const {isOperationDefinitionAST} = SchemaUtils;

export type GenerateExtraFiles = (
  getOutputDirectory: (path?: string) => CodegenDirectory,
  compilerContext: CompilerContext,
  getGeneratedDirectory: (definitionName: string) => CodegenDirectory,
) => void;

export type ValidationRule = (context: ValidationContext) => any;

export type WriterConfig = {
  baseDir: string,
  compilerTransforms: CompilerTransforms,
  customScalars: ScalarTypeMapping,
  formatModule: FormatModule,
  generateExtraFiles?: GenerateExtraFiles,
  inputFieldWhiteListForFlow: Array<string>,
  outputDir?: string,
  persistQuery?: (text: string) => Promise<string>,
  platform?: string,
  relayRuntimeModule?: string,
  schemaExtensions: Array<string>,
  useHaste: boolean,
  // Haste style module that exports flow types for GraphQL enums.
  // TODO(T22422153) support non-haste environments
  enumsHasteModule?: string,
  validationRules?: {
    GLOBAL_RULES?: Array<ValidationRule>,
    LOCAL_RULES?: Array<ValidationRule>,
  },
  packageName?: string,
  useNulls?: boolean
};

class ScalaFileWriter implements FileWriterInterface {
  _onlyValidate: boolean;
  _config: WriterConfig;
  _baseSchema: GraphQLSchema;
  _baseDocuments: ImmutableMap<string, DocumentNode>;
  _documents: ImmutableMap<string, DocumentNode>;
  _reporter: Reporter;

  constructor({
    config,
    onlyValidate,
    baseDocuments,
    documents,
    schema,
    reporter,
  }: {
    config: WriterConfig,
    onlyValidate: boolean,
    baseDocuments: ImmutableMap<string, DocumentNode>,
    documents: ImmutableMap<string, DocumentNode>,
    schema: GraphQLSchema,
    reporter: Reporter,
  }) {
    this._baseDocuments = baseDocuments || ImmutableMap();
    this._baseSchema = schema;
    this._config = config;
    this._documents = documents;
    this._onlyValidate = onlyValidate;
    this._reporter = reporter;

    validateConfig(this._config);
  }

  async writeAll(): Promise<Map<string, CodegenDirectory>> {
    // Can't convert to IR unless the schema already has Relay-local extensions
    const transformedSchema = ASTConvert.transformASTSchema(
      this._baseSchema,
      this._config.schemaExtensions,
    );
    const extendedSchema = ASTConvert.extendASTSchema(
      transformedSchema,
      this._baseDocuments
        .merge(this._documents)
        .valueSeq()
        .toArray(),
    );

    // Build a context from all the documents
    const baseDefinitionNames = new Set();
    this._baseDocuments.forEach(doc => {
      doc.definitions.forEach(def => {
        if (isOperationDefinitionAST(def) && def.name) {
          baseDefinitionNames.add(def.name.value);
        }
      });
    });
    const definitionDirectories = new Map();
    const allOutputDirectories: Map<string, CodegenDirectory> = new Map();
    const addCodegenDir = dirPath => {
      const codegenDir = new CodegenDirectory(dirPath, {
        onlyValidate: this._onlyValidate,
      });
      allOutputDirectories.set(dirPath, codegenDir);
      return codegenDir;
    };

    let configOutputDirectory;
    if (this._config.outputDir) {
      configOutputDirectory = addCodegenDir(this._config.outputDir);
    } else {
      this._documents.forEach((doc, filePath) => {
        doc.definitions.forEach(def => {
          if (isOperationDefinitionAST(def) && def.name) {
            definitionDirectories.set(
              def.name.value,
              path.join(this._config.baseDir, path.dirname(filePath)),
            );
          }
        });
      });
    }

    const packageName = this._config.packageName;

    const definitions = ASTConvert.convertASTDocumentsWithBase(
      extendedSchema,
      this._baseDocuments.valueSeq().toArray(),
      this._documents.valueSeq().toArray(),
      // Verify using local and global rules, can run global verifications here
      // because all files are processed together
      [...RelayValidator.LOCAL_RULES, ...RelayValidator.GLOBAL_RULES],
      RelayParser.transform.bind(RelayParser),
    );

    const compilerContext = new CompilerContext(
      this._baseSchema,
      extendedSchema
    ).addAll(definitions);

    const getGeneratedDirectory = definitionName => {
      if (configOutputDirectory) {
        return configOutputDirectory;
      }
      const definitionDir = definitionDirectories.get(definitionName);
      invariant(
        definitionDir,
        'RelayFileWriter: Could not determine source directory for definition: %s',
        definitionName,
      );
      const generatedPath = path.join(definitionDir, '__generated__');
      let cachedDir = allOutputDirectories.get(generatedPath);
      if (!cachedDir) {
        cachedDir = addCodegenDir(generatedPath);
      }
      return cachedDir;
    };


    const transformedFlowContext = compilerContext.applyTransforms(
      RelayFlowGenerator.flowTransforms,
      this._reporter,
    );

    const transformedQueryContext = compilerContext.applyTransforms(
      [
        ...this._config.compilerTransforms.commonTransforms,
        ...this._config.compilerTransforms.queryTransforms,
      ],
      this._reporter,
    );

    const artifacts = compileRelayArtifacts(
      compilerContext,
      this._config.compilerTransforms,
      this._reporter,
    );

    const existingFragmentNames = new Set(
      definitions.map(definition => definition.name),
    );

    // TODO(T22651734): improve this to correctly account for fragments that
    // have generated flow types.
    baseDefinitionNames.forEach(baseDefinitionName => {
      existingFragmentNames.delete(baseDefinitionName);
    });

    try {
      await Promise.all(
        artifacts.map(async node => {
          if (baseDefinitionNames.has(node.name)) {
            // don't add definitions that were part of base context
            return;
          }

          const relayRuntimeModule =
            this._config.relayRuntimeModule || 'relay-runtime';

          const flowNode = transformedFlowContext.get(node.name);
          invariant(
            flowNode,
            'RelayCompiler: did not compile definition: %s',
            node.name,
          );

          // console.log(artifacts);

          const flowTypes = RelayFlowGenerator.generate(flowNode, {
            customScalars: this._config.customScalars,
            enumsHasteModule: this._config.enumsHasteModule,
            existingFragmentNames,
            inputFieldWhiteList: this._config.inputFieldWhiteListForFlow,
            relayRuntimeModule,
            useHaste: this._config.useHaste,
            noFutureProofEnums: false,
            nodes: transformedFlowContext,
            useNulls: this._config.useNulls
          });

          await writeRelayScalaFile(
            getGeneratedDirectory(node.name),
            node,
            this._config.formatModule,
            flowTypes.core,
            this._config.persistQuery,
            this._config.platform,
            relayRuntimeModule,
            packageName,
            flowTypes.supporting,
            flowTypes.implicits,
            flowTypes.objectParent
          );
        }),
      );

      if (this._config.generateExtraFiles) {
        const configDirectory = this._config.outputDir;
        this._config.generateExtraFiles(
          dir => {
            const outputDirectory = dir || configDirectory;
            invariant(
              outputDirectory,
              'ScalaFileWriter: cannot generate extra files without specifying ' +
                'an outputDir in the config or passing it in.',
            );
            let outputDir = allOutputDirectories.get(outputDirectory);
            if (!outputDir) {
              outputDir = addCodegenDir(outputDirectory);
            }
            return outputDir;
          },
          transformedQueryContext,
          getGeneratedDirectory,
        );
      }

      // clean output directories
      allOutputDirectories.forEach(dir => {
        dir.deleteExtraFiles();
      });
    } catch (error) {
      let details;
      try {
        details = JSON.parse(error.message);
      } catch (_) {}
      if (details && details.name === 'GraphQL2Exception' && details.message) {
        throw new Error('GraphQL error writing modules:\n' + details.message);
      }
      throw new Error('Error writing modules:\n' + error.toString());
    }

    return allOutputDirectories;
  }
}

function validateConfig(config: Object): void {
  if (config.buildCommand) {
    process.stderr.write(
      'WARNING: ScalaFileWriter: For ScalaFileWriter to work you must ' +
        'replace config.buildCommand with config.formatModule.\n',
    );
  }
}

module.exports = ScalaFileWriter;