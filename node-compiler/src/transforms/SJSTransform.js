/**
 *  @flow
 */

const GraphQLCompilerContext = require('relay-compiler/lib/GraphQLCompilerContext');
const GraphQLIRTransformer = require('relay-compiler/lib/GraphQLIRTransformer');

const invariant = require('invariant');

import type {InlineFragment, Fragment, FragmentSpread, Directive} from 'relay-compiler/lib/GraphQLIR';


const {
  CompilerContext,
  IRTransformer,
  getLiteralArgumentValues,
} = require('relay-compiler/lib/GraphQLCompilerPublic');

function sjsTransform(
  context: GraphQLCompilerContext,
): GraphQLCompilerContext {

  return IRTransformer.transform(
    context,
    {
      Fragment: visitFragment,
      FragmentSpread: visitFragmentSpread,
    },
    () => ({}),
  );
}

const SJS = 'scalajs';

function visitFragment(fragment: Fragment): Fragment {
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

function visitFragmentSpread(fragment: Fragment): Fragment {
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