/**
 * Copyright (c) 2013-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @providesModule RelayFlowGenerator
 * @flow
 * @format
 */

'use strict';

// const t = require('babel-types');

const IRVisitor = require('relay-compiler').IRVisitor;
const Profiler = require('relay-compiler').Profiler;
// console.log(Profiler);


const RelayRelayDirectiveTransform = require('relay-compiler/lib/transforms/RelayDirectiveTransform');
const RelayMaskTransform = require('relay-compiler/lib/transforms/MaskTransform');
const RelayMatchTransform = require('relay-compiler/lib/transforms/MatchTransform');
// TODO: Look at adding these
const FlattenTransform = require("relay-compiler/lib/transforms/FlattenTransform");
const RequiredFieldTransform = require('relay-compiler/lib/transforms/RequiredFieldTransform');
const RelayRefetchableFragmentTransform = require('relay-compiler/lib/transforms/RefetchableFragmentTransform');


import type {Schema, TypeID} from "relay-compiler/core/Schema";

import type {Fragment, IRTransform, Root,} from 'relay-compiler/lib/RelayCompilerPublic';

import type {
  ConcreteCondition,
  ConcreteFragment,
  ConcreteFragmentSpread,
  ConcreteInlineFragment,
  ConcreteLinkedField,
  ConcreteNode,
  ConcreteRoot,
} from 'react-relay';


import type {ScalarTypeMapping} from 'relay-compiler/lib/RelayFlowTypeTransformers';

const invariant = require('invariant');

const SJSTransform = require('../transforms/SJSTransform');

const ARRAY_MOD = "array"
const OPTIONAL_MOD = "optional"
const INPUT_MOD = "inputobj"

/*
Better typing ideas here.

https://github.com/facebook/relay/issues/1918

*/

type Options = {|
  +customScalars: ScalarTypeMapping,
  +useHaste: boolean,
  +enumsHasteModule: ?string,
  +existingFragmentNames: Set<string>,
  +inputFieldWhiteList: $ReadOnlyArray<string>,
  +relayRuntimeModule: string,
  +noFutureProofEnums: boolean,
  nodes: Map<string, Root | Fragment>,
  useNulls?: boolean,
|};

function generate(
  schema: Schema,
  node: Root | Fragment,
  options: Options,
): string {

  const newCT = new ClassTracker(schema, options.nodes, options.useNulls || true, options.customScalars);
  try {
    IRVisitor.visit(
      node,
      createVisitor(schema, newCT)
    )
    const code = newCT.out();

    const metadata = node.metadata || {}
    const responseType = metadata.plural ? "type Response[T] = js.Array[T]" : "type Response[T] = T";

    return `
${code.core}

object ${node.name} extends ${code.objectParent || '_root_.relay.gql.GenericGraphQLTaggedNode'} {
        ${responseType}

        ////////////////////////////////////
        ////// Supporting classes begin here
        ////////////////////////////////////

        ${code.supporting || ''}

        ///////////////////////////
        ////// Implicits begin here
        ///////////////////////////
        ${code.implicits || ''}

    `;

  } catch (e) {
    console.error(e);
    throw e;
  }
}

type ASpread = {
  members: Array<Member>,
  extendCls: Array<string>,
}

type ATpe = {
  name: string,
  mods: Array<string>,
  members?: Array<Member>,
  fieldName?: string,
}

type Member = {
  name: string,
  tpe: Array<ATpe>,
  comments: Array<string>,
  node?: ?ConcreteNode,
  parentFrag?: ?string,
  or?: ?boolean,
};

type Cls = {
  name: string,
  members: Array<Member>,
  extendsC: Array<string>,
  open: boolean,
  linkedField: boolean,
  spreadFrags: Array<string>,
  isClass?: boolean,
  useNulls: boolean,
  useVars?: boolean,
  parentObj?: string,
};

type ImplDef = {
  from: string,
  to: string,
  name: string,
  inline: boolean,
}

type QueryType = {
  input: string,
  output: string,
}

class ClassTracker {

  classes: Map<string, Cls>;
  topClasses: Map<string, Cls>;
  implicits: Array<ImplDef>;
  fields: Array<Member>;
  spreads: Array<[string, ASpread]>;
  topLevelTypeParams: Array<QueryType>;
  isQuery: boolean;
  isMutation: boolean;
  isFragment: boolean;
  isRefetchable: boolean;
  isSubscription: boolean;
  _nodes: Map<string, ConcreteRoot | ConcreteFragment>;
  _useNulls: boolean;
  factoryMethods: Array<[string, Cls]>;
  schema: Schema;

  constructor(schema: Schema, nodes: Map<string, ConcreteRoot | ConcreteFragment>, useNulls: boolean, customScalars: ScalarTypeMapping) {
    this.classes = new Map();
    this.topClasses = new Map();
    this.fields = [];
    this.spreads = [];
    this._nodes = nodes;
    this.implicits = [];
    this.topLevelTypeParams = [];
    this.isQuery = false;
    this.isMutation = false;
    this.isFragment = false;
    this.isRefetchable = false;
    this.isSubscription = false;
    this._useNulls = useNulls;
    this.factoryMethods = [];
    this.schema = schema;
    this.customScalars = customScalars;
  }

  newClassName(cn: string, n: ?number): string {
    const name = n ? `${cn}_${n}` : cn;
    const cls = this.classes.get(name);

    if (cls) {
      const num = n ? n + 1 : 1;
      return this.newClassName(cn, num);
    } else {
      return name;
    }
  }

  newImplicit(from: string, to: string, name: string, inline: boolean) {
    this.implicits.unshift({from, to, name, inline});
  }

  newQueryTypeParam(input: string, output: string , refetchIn?: string, refetchOut?: string) {
    this.topLevelTypeParams.push({input, output, refetchIn, refetchOut});
  }

  /**
   * Create a new class definitively.  It could be the result of a partial selection or a fragment spread.
   *
   * Linked Field means a partial/full selection.
   *
   * @param {string} cn
   * @param {*} members
   * @param {*} extendsC
   * @param {*} linkedField
   * @param {*} spreadFrags
   * @param useNulls set globally or locally
   * @param useVars todo
   */
  newClass(cn: string, members: Array<Member>, extendsC: Array<string>, linkedField: boolean, spreadFrags: Array<string>, useNulls: boolean, useVars?: boolean): string {
    let name = cn;
    if (linkedField) {
      name = this.newClassName(cn);
      const cls = {
        name,
        members,
        extendsC,
        linkedField,
        open: false,
        spreadFrags,
        useNulls,
        useVars,
      }
      this.classes.set(name, cls);
    } else {
      const cls = {
        name,
        members,
        extendsC,
        linkedField,
        open: false,
        spreadFrags,
        useNulls,
        useVars,
      }
      this.topClasses.set(name, cls);
    }
    return name;
  }

  newFactoryMethod(methodName: string,
                   name: string,
                   members: Array<Member>,
                   extendsC: Array<string>,
                   linkedField: boolean,
                   spreadFrags: Array<string>,
                   useNulls: boolean,
                   useVars?: boolean,
                   parentObj?: String) {
    this.factoryMethods.push([methodName, {
      name,
      members,
      extendsC,
      linkedField,
      open: false,
      spreadFrags,
      useNulls,
      useVars,
      parentObj
    }]);
  }

  getNewTpeInlineFrag(node: ConcreteInlineFragment): string {
    if (node.parentTpe) {
      /* $FlowFixMe */
      return node.parentTpe.slice(1).join("") + node.typeCondition.toString();
    }
    /* $FlowFixMe */
    throw new Error(`Shouldn't happen, ${node.typeCondition.toString} parent: ${node.parentTpe}`);
  }

  /**
   * Valid for any type.
   * @param {*} node
   */
  getNewTpeParent(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment | ConcreteInlineFragment): string {
    if (node.parentTpe && node.parentTpe.length > 1) /* $FlowFixMe */
      return node.parentTpe.slice(1).join("");
    else /* $FlowFixMe */
      return node.parentTpe.join("");
  }

  getNodeName(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment): string {
    // TODO: Should I change this?
    const suffix = node.operation === "query" ? "" : "";
    // $FlowFixMe
    return (node.alias || node.name) + suffix;
  }

  getNewTpe(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment): string {
    const nameAlias = this.getNodeName(node);
    if (node.kind === "LinkedField") {
      if (node.parentTpe) { /* $FlowFixMe */
        return node.parentTpe.slice(1).join("") + titleCase(nameAlias);
      }
    }
    return titleCase(nameAlias);
  }

  /**
   * Handle inline frags.
   */
  inlineFrag(node: ConcreteInlineFragment) {
    const newClassName: string = this.getNewTpeInlineFrag(node);
    const selectionMembers = node.selections.filter(s => s.kind !== "FragmentSpread" && s.kind !== "InlineFragment");
    const fragSpreadMembers = node.selections.filter(s => s.kind === "FragmentSpread");
    const listOfSpreads: Array<[string, ASpread]> = fragSpreadMembers.map(_ => this.popSpread());
    // Using selection members to pull the right amount of fields out.
    const localMembers: Array<Member> = selectionMembers.map(() => this.popMember());
    const extendsCls = this.getScalajsDirectiveExtends(node);
    const newTpe = this.newClass(newClassName, localMembers, extendsCls, true, [], node.useNulls);
    const parentCls = this.getNewTpeParent(node);

    listOfSpreads.forEach(([k, {members}]) => {
      this.newImplicit(newClassName, k, k, false);
    });

    /* $FlowFixMe */
    this.newImplicit(parentCls, newTpe, `${node.typeCondition.toString()}`, true);
    node.selections;
  }

  getScalajsDirectiveExtends(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment | ConcreteInlineFragment | ConcreteFragmentSpread): Array<string> {
    // $FlowFixMe
    return (node.metadata && node.metadata.extends && [node.metadata.extends]) || [];
  }

  isUseNulls(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment | ConcreteInlineFragment | ConcreteFragmentSpread): boolean {
    // $FlowFixMe
    return !!(this._useNulls || node.useNulls || (node.metadata && node.metadata.useNulls));
  }

  /**
   * TODO: This needs to severely be cleaned up, wayyyy too much going on.
   *
   *
   *
   * @param {*} node
   * @param {*} linked is it a linked field
   * @param {*} root is it the root?
   */
  closeField(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment, linked: boolean, root: boolean) {
    //$FlowFixMe
    const spreadFrags: Array<ConcreteFragmentSpread> = node.selections.filter(s => s.kind === "FragmentSpread");
    // $FlowFixMe
    const inlineFrags: Array<ConcreteInlineFragment> = node.selections.filter(s => s.kind === "InlineFragment");

    // Don't look at Inline Fragments or Fragment Spreads, they get treated differently.
    const selectionMembers = node.selections.filter(s => s.kind !== "FragmentSpread" && s.kind !== "InlineFragment");

    // We modify this in the baton.  Pop all the members off.
    let localMembers: Array<Member> = selectionMembers.map(() => this.popMember());

    if (!node.type) {
      throw new Error(`Expected ${node.toString()} to have a type member.`)
    }


    if (inlineFrags.length > 0) {
      // if we don't have __typename in the set of selection members add it synthetically since the relay compiler will
      // anyways. $FlowFixMe
      if (!selectionMembers.find(f => f.name === "__typename"))
        localMembers.push({name: "__typename", tpe: [{name: "String", mods: []}], comments: []});
    }

    const listOfSpreads: Array<[string, ASpread]> = spreadFrags.map(_ => this.popSpread());

    // $FlowFixMe
    const fieldName: string = node.alias ? node.alias : node.name;
    const newClassName: string = this.getNewTpe(node);

    // $FlowFixMe
    const spreadParentsFrags: Array<string> =
      Array.from(new Set(flattenArray(listOfSpreads.map(s => s[1].members.filter(s => !!s.parentFrag).map(s => s.parentFrag)))));


    // flattenArray(listOfSpreads.map(s => s[1].extendCls));
    const fieldExtend: Array<string> = this.getScalajsDirectiveExtends(node);

    const newTpe = this.newClass(newClassName, localMembers, fieldExtend, linked, spreadParentsFrags, node.useNulls);

    listOfSpreads.forEach(([k, {members}]) => {
      this.newImplicit(newTpe, k, k, false);
    });

    /* $FlowFixMe */
    const newNewTpe = this.transformScalarType(node.type, newTpe);
    let comments: Array<string>;

    if (node.selections.length === selectionMembers.length) {
      // No spreads.
      comments = ["No spreads, just combining."];
    } else {
      // Spreads and members that are conflicting
      comments = ["New fields added, conflicts detected."];
    }

    this.newMember({
      name: fieldName,
      tpe: newNewTpe,
      comments,
    });
  }

  inputName(name: string) {
    return name + "Input"
  }

  handleQuery(node: ConcreteRoot) {
    const clsName = this.inputName(node.name);
    const newClasses: Array<Cls> = [];
    if (node.operation === 'query') {
      const members: Array<Member> = node.argumentDefinitions.map(({name, type}) => {
        // $FlowFixMe
        const tpe = this.transformInputType(type, newClasses, node.name);
        return {name, tpe, comments: []};
      });
      const resultName = this.newClass(clsName, members, [], false, [], node.useNulls, false);
      this.newFactoryMethod("newInput", resultName, members, [], false, [], true, false);
      this.newQueryTypeParam(resultName, node.name);
      this.isQuery = true;
    } else if (node.operation === 'mutation') {
      // We diverge from normal graphql norms, to basically skip the { input: ...blah } part of the mutation
      // Because honestly, its superfluous

      // console.log(node.argumentDefinitions)
      const clsMembers: Array<Member> = node.argumentDefinitions.map(({name, type}) => {
        // $FlowFixMe
        const tpe = this.transformInputType(type, newClasses, node.name);
        return {name, tpe, comments: []};
      });

      if (clsMembers.length === 0) {
        this.newQueryTypeParam("Null", node.name);
      } else {
        if (newClasses.length === 0) {
          // A scalar input
          const cls = clsMembers[0].tpe[0]

          this.newQueryTypeParam(cls.name, node.name);
        } else {

          // console.log(clsMembers, newClasses)
          const innerClass = newClasses.pop()

          // const resultName = this.newClass(clsName, clsMembers, [], false, [], node.useNulls, false);
          // this.newFactoryMethod("newInput", resultName, clsMembers, [], false, [], true, false);
          // this.newQueryTypeParam(resultName, node.name);
          const { members, extendsC, linkedField, spreadFrags, useVars} = innerClass

          const resultName = this.newClass(clsName, members, extendsC, linkedField, spreadFrags, node.useNulls, useVars);
          this.newFactoryMethod("newInput", resultName, members, [], false, [], true, false);
          this.newQueryTypeParam(resultName, node.name);
        }
      }
      this.isMutation = true;
    } else if (node.operation === 'subscription') {
      const members: Array<Member> = node.argumentDefinitions.map(({name, type}) => {
        // $FlowFixMe
        const tpe = this.transformInputType(type, newClasses, node.name);
        return {name, tpe, comments: []};
      });

      const resultName = this.newClass(clsName, members, [], false, [], node.useNulls, false);
      this.newFactoryMethod("newInput", resultName, members, [], false, [], true, false);
      this.newQueryTypeParam(resultName, node.name);
      this.isSubscription = true;
    }
    newClasses.forEach(({name, members, extendsC, linkedField, spreadFrags, useVars}) => {
      this.newClass(name || '', members, extendsC, linkedField, spreadFrags, node.useNulls, false);
    });
    // console.log(node);
  }

  handleFragment(node: ConcreteFragment) {
    this.isFragment = true
    if (node.metadata && node.metadata.refetch && node.metadata.refetch.operation) {
      this.isRefetchable = true;
      const opName = node.metadata.refetch.operation
      const refetchIn = this.inputName(opName)
      this.newQueryTypeParam(undefined, node.name, refetchIn, opName)
    } else {
      this.newQueryTypeParam(undefined, node.name)
    }
  }

  newSpread(n: ConcreteFragmentSpread) {
    // $FlowFixMe
    const tpe = this.getNewTpe(n);
    const extendCls: Array<string> = this.getScalajsDirectiveExtends(n);

    // const dm = this.getDirectMembersForFrag(n.name, tpe).map(s => {
    //   s.or = true;
    //   return s;
    // });
    this.spreads.unshift([n.name, {
      members: [],
      extendCls,
    }]);
  }

  newMember(m: Member): void {
    this.fields.unshift(m);
  }

  popMember(): Member {
    return this.fields.shift();
  }

  popSpread(): [string, ASpread] {
    return this.spreads.shift();
  }

  transformNonNullableScalarType(
    type: TypeID,
    backupType: ?string
  ): Array<ATpe> {
    if (this.schema.isList(type)) {
      return this.transformScalarType(this.schema.getListItemType(type), backupType).map(s => {

        s.mods.push(ARRAY_MOD);
        return s;
      });
    } else if (
      this.schema.isObject(type) ||
      this.schema.isUnion(type) ||
      this.schema.isInterface(type)
    ) {
      if (!backupType) {
        throw new Error(`Could not convert from GraphQL type ${type.toString()}, missing backup type`);
      }
      return [{name: backupType, mods: []}];
    } else if (this.schema.isScalar(type)) {
      return this.transformGraphQLScalarType(this.schema.getTypeString(type), backupType);
    } else if (this.schema.isEnum(type)) {
      return [{name: "String", mods: []}];
    } else {
      throw new Error(`Could not convert from GraphQL type ${type.toString()}, ${type.prototype}`);
    }
  }

  transformInputType(type: TypeID, classes: Array<Cls>, prefix: string): Array<ATpe> {
    if (this.schema.isNonNull(type)) {
      return this.transformNonNullableInputType(this.schema.getNullableType(type), classes, prefix);
    } else {
      return this.transformNonNullableInputType(type, classes, prefix).map(s => {
        s.mods.push(OPTIONAL_MOD)
        return s;
      });
    }
  }

  transformNonNullableInputType(type: TypeID, classes: Array<Cls>, prefix: string): Array<ATpe> {
    if (this.schema.isList(type)) {
      return this.transformInputType(this.schema.getListItemType(type), classes, prefix).map(s => {
        s.mods.push(ARRAY_MOD);
        return s;
      });
    } else if (this.schema.isScalar(type)) {
      return this.transformGraphQLScalarType(this.schema.getTypeString(type));
    } else if (this.schema.isEnum(type)) {
      return [{name: "String", mods: []}];
    } else if (this.schema.isInputObject(type)) {
      const fields = this.schema.getFields(type);
      const props: Array<Member> = fields.map((fieldID: FieldID) => {
        const fieldType = this.schema.getFieldType(fieldID);
        const fieldName = this.schema.getFieldName(fieldID);
        const property = this.transformInputType(fieldType, classes, prefix)
        return {
          name: fieldName,
          tpe: property,
          comments: [],
        };
      });

      if(!classes.find((x) => x.name == prefix + type.toString())) {
        classes.push({
          name: prefix + type.toString(),
          members: props,
          open: false,
          extendsC: [],
          linkedField: false,
          spreadFrags: [],
          useNulls: true,
        });

        this.newFactoryMethod("apply", prefix + type.toString(), props, [], false, [], true, false, prefix + type.toString())
      }

      return [{
        name: prefix + type.toString(),
        mods: [INPUT_MOD],
      }];
    } else {
      throw new Error(`Could not convert from GraphQL Input type ${type.toString()}`);
    }
  }

  transformGraphQLScalarType(type: String, backupType: ?string): Array<ATpe> {
    const customType = this.customScalars[type];
    if (typeof customType === 'function') {
      throw new Error('Don\'t know how to deal with a function here');
    }
    switch (type) {
      case 'ID':
      case 'String':
      case 'Url':
        return [{name: "String", mods: []}];
      case 'Int':
        return [{name: "Int", mods: []}];
      case 'Float':
      case 'BigDecimal':
      case 'BigInt':
      case 'Long':
        return [{name: "Double", mods: []}];
      case 'Boolean':
        return [{name: "Boolean", mods: []}];
      default:
        if (backupType) {
          return [{name: backupType, mods: []}];
        }
        if (customType) {
          return [{name: customType, mods: []}];
        }
        return [{name: `js.Any`, mods: []}];
    }
  }

  transformScalarType(
    type: TypeID,
    backupType: ?string,
  ): Array<ATpe> {
    if (this.schema.isNonNull(type)) {
      return this.transformNonNullableScalarType(this.schema.getNullableType(type), backupType);
    } else {
      // $FlowFixMe
      return this.transformNonNullableScalarType(type, backupType).map(s => {
        s.mods.push(OPTIONAL_MOD);
        return s;
      });
    }
  }


  getDirectMembersForFrag(name: string, backupType: ?string): Array<Member> {
    const node = this._nodes.get(name);
    // console.log(node);
    if (node) {
      return flattenArray(node.selections.map(s => {
        if (s.kind === "FragmentSpread") {
          // $FlowFixMe
          return this.getDirectMembersForFrag(s.name, name + "." + titleCase(s.name));
        } else {
          return [{
            // $FlowFixMe
            name: s.name,
            // $FlowFixMe
            tpe: this.transformScalarType(s.type, name + "." + titleCase(s.name)),
            comments: [`getDirectMembersForFrag child of ${name}`],
            parentFrag: name,
            scalar: s.kind === "ScalarField",
          }];
        }
      }));
    } else return [];
  }

  makeType(className: string, s: ATpe, cs?: Set<string>, useNulls: boolean): string {
    let newTpeName = cs && cs.has(s.name) ? className + "." + s.name : s.name;
    s.mods.forEach(mod => {
      if (mod === ARRAY_MOD) {
        newTpeName = "js.Array[" + newTpeName + "]";
      } else if (mod === OPTIONAL_MOD) {
        if (useNulls) {
          newTpeName = `${newTpeName} | Null`
        } else {
          // Don't do anything for now.
        }
      } else if (mod === INPUT_MOD) {
        // if (s.members) {
        //   newTpeName = s.members.map(mem => {
        //     return this.makeTypeFromMember(s.name, mem);
        //   }).join("\n");
        // }
      }
    });
    return newTpeName;
  }

  makeTypeFromMember(className: string, m: Member, otherClasses?: Map<string, Cls>, useNulls: boolean): string {
    const joinStr = m.or ? " | " : " with ";
    let tpe = "";
    if (otherClasses) {
      // Declared new classes.
      const cs = new Set(otherClasses.keys());
      tpe = m.tpe.map(s => {
        return this.makeType(className, s, cs, useNulls);
      }).join(joinStr);
    } else {
      tpe = m.tpe.map(s => {
        return this.makeType(className, s, undefined, useNulls);
      }).join(joinStr);
    }
    return tpe;
  }

  getClientTypeAnnotationFromSchema(node: ConcreteRoot) {
    const maybeField = this.schema.getFieldByName(this.schema.getNullableType(this.schema.getListItemType(this.schema.getNullableType(node.parentNode.type))), node.name)
    if (maybeField && maybeField.directives) {
      const result = maybeField.directives.filter(({name}) => {
        return name === "scalajs"
      })
      .flatMap(directive => directive.args
        .filter(({name}) => name === "clientType")
        .map(({value}) => {
          return value.value
        })
      )
      return result[0]
    }
  }

  outTrait({name, members, extendsC, open, isClass, useNulls, useVars}: Cls, otherClasses?: Map<string, Cls>): string {
    invariant(name, "Name needs to be set");
    const indent = otherClasses ? "" : "  ";

    const declPrefix = useVars ? "  var" : "  val";
    const ex = extendsC.length > 0 ? ["with", extendsC.join(" with ")] : [];
    const cls = indent + [`trait`, name, "extends", "js.Object", ...ex, "{"].join(" ");

    const outMembers = Array.from(members.values()).filter(s => !!s).map(s => {
      const comment = s.comments.length === 0 ? [] : ["  /**", ...s.comments, "*/"];

      // Figure out if we've created the type, if so, add a prefix.
      const tpe = this.makeTypeFromMember(name, s, otherClasses, useNulls);
      const commentOut = comment.length > 0 ? indent + comment.join(" ") + "\n" : "";
      return commentOut + [indent + declPrefix, s.name, ":", tpe].join(" ");
    }).join("\n");

    return [cls, outMembers, indent + "}"].join("\n");
  }

  /**
   * Group all the implicits, and create
   * @param {} impl
   */
  outImplicitsFrags(impl: Array<ImplDef>): string {

    const groups: { [string]: Array<ImplDef> } = {};
    impl.forEach((item) => {
      const list = groups[item.from];

      if (list) {
        list.push(item);
      } else {
        groups[item.from] = [item];
      }
    });

    return Object.keys(groups).map(k => {

      const items = groups[k];
      const indent = "  ";
      const members = Array.from(items).map(({from, to, name}) => {
        return [
          `${indent}  def as${name}: Option[${to}] = {`,
          `${indent}    if (f == null) None`,
          `${indent}    else if (js.isUndefined(f.__typename)) {`,
          `${indent}      if (_root_.scala.scalajs.LinkingInfo.developmentMode) {`,
          `${indent}        _root_.org.scalajs.dom.console.warn("Did you forget to include __typename in your graphql definition?")`,
          `${indent}      }`,
          `${indent}      None`,
          `${indent}    }`,
          `${indent}    else if (f.__typename == "${name}") Some(f.asInstanceOf[${to}])`,
          `${indent}    else None`,
          `${indent}  }`
        ].join("\n");
      });
      return [`${indent}implicit class ${k}_Ops(f: ${k}) {`, ...members, `${indent}}`].join("\n");
    }).join("\n");

  }

  /**
   * This logic is fairly convoluted, but it accomplishes a couple different things,
   *  - It handles non inline conversions from a type to another.  The purpose of these
   *    types are to casting from one type directly to another.  Usually this is set by
   *    the logic in LinkedField or something similar
   *  - 2nd thing it does is handles converting top level objects to something that you can pass around
   *    I am not sure how sound this is though. **I explain below**
   *
   * @param {*} impl filtered list of implicits
   * @param {*} otherClasses the list of non top level classes in part of this root node
   * @param {*} topClasses the root node, usually a list of one.
   */
  outImplicitsConversion(impl: Array<ImplDef>, otherClasses?: ?Map<string, Cls>, topClasses: Array<Cls>): string {

    const ref = `_root_.relay.gql.FragmentRef`

    // Handle explicits implicits we've asked for.
    const text = impl.map(({from, to, name}) => {
      return [
        `  implicit class ${from}2${to}Ref(f: ${from}) extends _root_.relay.gql.CastToFragmentRef[${from}, ${to}](f) {`,
        `    def to${to}: ${ref}[${to}] = castToRef`,
        `  }`,
      ].join("\n");
    }).join("\n");
    return [text].join("\n");
  }

  /**
   * Output a factory method for generating a class
   * @param {string} methodName name of the method
   * @param {Cls} cls class description
   */
  outFactoryMethod(methodName: string, cls: Cls): string {
    const result = cls.members.map(m => {
      let rest = ""
      if (m.tpe[0].mods.find(f => f === OPTIONAL_MOD)) {
        rest = " = null"
      }

      return `${m.name}: ${this.makeTypeFromMember(cls.name, m, undefined, cls.useNulls)}${rest}`;
    }).join(", ");

    const body = [
      "js.Dynamic.literal(",
      cls.members.map(m => {
        return `    "${m.name}" -> ${m.name}.asInstanceOf[js.Any]`
      }).join(",\n"),
      `  ).asInstanceOf[${cls.name}]`
    ].join("\n");
    return `  def ${methodName}(${result}): ${cls.name} = ${body}`;
  }

  outFactoryObject(name: string, body: string) : string {
    return `object ${name} {
  ${body}
}`
  }

  out(): { supporting: string, core: string, implicits: string, objectParent: string } {
    // invariant(this.topClasses.size === 1, `there should be 1 "topClasses", got `);


    const supporting = Array.from(this.classes.entries())
      .map(s => s[1])
      .map(cls => {
        return this.outTrait(cls);
      })
      .join("\n");

    const implicits = [
      this.outImplicitsFrags(this.implicits.filter(s => s.inline)),
      this.outImplicitsConversion(this.implicits.filter(s => !s.inline), this.classes, Array.from(this.topClasses.values())),
      ...this.factoryMethods
        .filter(([met, cls]) => !cls.parentObj)
        .map(([method, cls]) => this.outFactoryMethod(method, cls))
    ].join("\n");


    if (this.isQuery || this.isMutation || this.isSubscription) {
      invariant(this.topLevelTypeParams.length <= 1, "Something went wrong and there are multiple input objects.");
    }

    let objectPrefix

    if (this.isQuery) {
      objectPrefix = "_root_.relay.gql.QueryTaggedNode"
    } else if (this.isMutation) {
      objectPrefix = "_root_.relay.gql.MutationTaggedNode"
    } else if (this.isSubscription) {
      objectPrefix = "_root_.relay.gql.SubscriptionTaggedNode"
    }
    else if (this.isFragment) {
      if (this.isRefetchable) {
        objectPrefix = "_root_.relay.gql.FragmentRefetchableTaggedNode"
      } else {
        objectPrefix = "_root_.relay.gql.FragmentTaggedNode"

      }
    } else {
      objectPrefix = ""
    }

    let objectParent
    if (this.topLevelTypeParams.length === 1) {
      // There is always an output
      if (this.topLevelTypeParams[0].input) {
        objectParent = `${objectPrefix}[${this.topLevelTypeParams[0].input}, ${this.topLevelTypeParams[0].output}]`
      } else {
        if (this.topLevelTypeParams[0].refetchIn) {
          objectParent = `${objectPrefix}[${this.topLevelTypeParams[0].output}, ${this.topLevelTypeParams[0].refetchIn}, ${this.topLevelTypeParams[0].refetchOut}]`
        } else {
          objectParent = `${objectPrefix}[${this.topLevelTypeParams[0].output}]`
        }
      }
    } else {
      objectParent = '_root_.relay.gql.GenericGraphQLTaggedNode';
    }

    return {
      core: Array.from(this.topClasses.entries()).map(s => {
        const cls = s[1];
        if (cls) {
          const supportingObjects = this.factoryMethods
            .filter(([met, maybeParent]) => maybeParent.parentObj === cls.name)
            .filter(([met, cls]) => cls.parentObj)
            .map(([method, cls]) => {

              return this.outFactoryObject(cls.parentObj, this.outFactoryMethod(method, cls))
            })
            .join("\n");

          return [this.outTrait(cls, this.classes), supportingObjects].join("\n");
        } else return "";
      }).join("\n"),
      supporting,
      implicits,
      objectParent
    };
  }
}

function createVisitor(schema: Schema, ct: ClassTracker) {
  return {
    enter: {
      Root(node: ConcreteRoot) {
        const useNulls = ct.isUseNulls(node);
        const selections = node.selections.map(s => {
          return {
            ...s,
            parentTpe: [node.name],
            parentNode: node,
            useNulls,
          }
        })
        return {
          ...node,
          selections,
          useNulls,
        };
      },
      Fragment(node: ConcreteFragment) {
        const useNulls = ct.isUseNulls(node);
        const selections = node.selections.map(s => {
          return {
            ...s,
            parentTpe: [node.name],
            parentNode: node,
            useNulls,
          }
        })
        return {
          ...node,
          selections,
          useNulls,
        };
      },
      InlineFragment(node: ConcreteInlineFragment) {
        // $FlowFixMe
        const {parentTpe} = node;
        const ptpe = parentTpe || [];
        const useNulls = ct.isUseNulls(node);
        const selections = node.selections.map(s => {
          return {
            ...s,
            parentTpe: ptpe,
            parentNode: node,
            useNulls,
          }
        })
        return {
          ...node,
          selections,
          useNulls,
        };
      },
      ScalarField(node) {
        // $FlowFixMe
        const useNulls = ct.isUseNulls(node);
        return {
          ...node,
          useNulls,
        };
      },
      LinkedField(node: ConcreteLinkedField) {
        // $FlowFixMe
        const {parentTpe} = node;
        const useNulls = ct.isUseNulls(node);
        const ptpe = parentTpe || [];
        const selections = node.selections.map(s => {
          return {
            ...s,
            parentNode: node,
            parentTpe: ptpe.concat([titleCase(node.name)]),
            useNulls,
          }
        })
        return {
          ...node,
          selections,
          useNulls,
        };
      },
      FragmentSpread(node: ConcreteFragmentSpread) {
        const useNulls = ct.isUseNulls(node);
        return {
          ...node,
          parentNode: node,
          useNulls,
        };
      },
      Condition(node: ConcreteCondition) {
        return node;
      },
      ModuleImport(node: any) {
        return node;
      },
    },
    leave: {
      Root(node: ConcreteRoot) {
        // console.log("Root", node);
        ct.handleQuery(node);
        ct.closeField(node, false, true);
        // // $FlowFixMe
        // // node.selections.map(s => ct.handleSelections(s));
        // ct.handleSelections(node);
        return node;
      },
      Fragment(node: ConcreteFragment) {
        // console.log("Fragment", node);
        ct.handleFragment(node)
        ct.closeField(node, false, false);
        // // $FlowFixMe
        // ct.handleSelections(node);
        return node;
      },
      InlineFragment(node: ConcreteInlineFragment) {
        // console.log("InlineFragment", node);
        // node.selections.map(s => s)
        ct.inlineFrag(node);
        return node;
      },
      ScalarField(node) {
        // console.log("ScalarField", node);
        // $FlowFixMe
        const typeCls: ?string = node.metadata && node.metadata.typeCls;
        const extendsCPresent: ?string = node.metadata && node.metadata.extends;
        const typeInfo = ct.getClientTypeAnnotationFromSchema(node)

        // $FlowFixMe
        const tpes = ct.transformScalarType((node.type: TypeID));
        if (typeCls) {
          tpes.map(tpe => {
            tpe.name = `${tpe.name}[${typeCls}]`
            return tpe;
          })
        } else {
          // Don't do both
          if (typeInfo) {
            tpes.map(tpe => {
              tpe.name = `${tpe.name}[${typeInfo}]`
            })
          }
        }

        // $FlowFixMe
        if (extendsCPresent) {
          const extendsArray = [{
            name: extendsCPresent,
            mods: []
          }];
          tpes.push(...extendsArray);
        }



        ct.newMember({name: node.alias || node.name, tpe: tpes, comments: []});
        return node;
      },
      LinkedField(node: ConcreteLinkedField) {
        // console.log("LinkedField", node);
        ct.closeField(node, true, false);
        return node;
      },
      FragmentSpread(node: ConcreteFragmentSpread) {
        // console.log("FragmentSpread", node);
        ct.newSpread(node);
        return node;
      },
      Condition(node: ConcreteCondition) {
        // console.log("Condition", node);
        return node;
      },
      ModuleImport(node: any) {
        // console.log("ModuleImport", node);
        return node;
      },
    }
  }
}


function titleCase(s: string): string {
  return s.charAt(0).toUpperCase() + s.substr(1);
}

function flattenArray<T>(arrayOfArrays: Array<Array<T>>): Array<T> {
  const result = [];
  arrayOfArrays.forEach(array => result.push(...array));
  return result;
}

const FLOW_TRANSFORMS: Array<IRTransform> = [
  RelayRelayDirectiveTransform.transform,
  RelayMaskTransform.transform,
  // ConnectionFieldTransform.transform,
  RelayMatchTransform.transform,
  RequiredFieldTransform.transform,
  FlattenTransform.transformWithOptions({}),
  RelayRefetchableFragmentTransform.transform,
  SJSTransform.transform,
  SJSTransform.transformRemoveSjs,
];

const schemaExtensions: Array<string> = [
  SJSTransform.SCHEMA_EXTENSION
]


module.exports = {
  generate: Profiler.instrument(generate, 'RelayScalaGenerator.generate'),
  transforms: FLOW_TRANSFORMS,
  schemaExtensions,
};
