/**
 *  @flow
 */

const GraphQLCompilerContext = require('relay-compiler/lib/GraphQLCompilerContext');
const GraphQLIRTransformer = require('relay-compiler/lib/GraphQLIRTransformer');

const invariant = require('invariant');

import type {InlineFragment, Fragment, FragmentSpread, Directive, Field} from 'relay-compiler/lib/GraphQLIR';


const {
  CompilerContext,
  IRTransformer,
  getLiteralArgumentValues,
} = require('relay-compiler');

function sjsTransform(
  context: GraphQLCompilerContext,
): GraphQLCompilerContext {

  return IRTransformer.transform(
    context,
    {
      Fragment: visitField,
      Root: visitField,
      FragmentSpread: visitField,
      InlineFragment: visitField,
      LinkedField: visitField,
      ScalarField: visitField,
    },
    () => ({}),
  );
}

const SJS = 'scalajs';

function visitField(fragment): any {
  let transformedFrag = this.traverse(fragment);
  const relayDirective = transformedFrag.directives.find(({name}) => name === SJS);
  if (!relayDirective) {
    return transformedFrag;
  }

  const arg = getLiteralArgumentValues(relayDirective.args);

  return {
    ...transformedFrag,
    directives: transformedFrag.directives.filter(
      directive => directive.name !== SJS,
    ),
    metadata: {
      ...(transformedFrag.metadata || {}),
      ...arg,
    },
  };
}

function filterDirectivesTransform(
  context: GraphQLCompilerContext,
): GraphQLCompilerContext {
  return GraphQLIRTransformer.transform(
    context,
    {Directive: visitDirective},
    () => ({}),
  );
}

/**
 *
 * Skip directives not defined in the original schema.
 */
function visitDirective(directive: Directive): ?Directive {
  if (directive.name === SJS)
    return null;
  return directive;
}


module.exports = {
  transform: sjsTransform,
  transformRemoveSjs: filterDirectivesTransform,
};