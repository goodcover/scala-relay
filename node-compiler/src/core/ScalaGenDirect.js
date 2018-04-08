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

const t = require('babel-types');

const {
  FlattenTransform,
  IRVisitor,
  SchemaUtils,
  Profiler,
} = require('relay-compiler/lib/GraphQLCompilerPublic');

const RelayMaskTransform = require('relay-compiler/lib/RelayMaskTransform');
const RelayRelayDirectiveTransform = require('relay-compiler/lib/RelayRelayDirectiveTransform');

import type {
  IRTransform,
  Fragment,
  Root,
  CompilerContext,
} from 'relay-compiler/lib/GraphQLCompilerPublic';

import type {
  ConcreteNode,
  ConcreteRoot,
  ConcreteFragment,
  ConcreteBatch,
  ConcreteSelection,
  ConcreteScalarField,
  ConcreteLinkedField,
  ConcreteFragmentSpread,
  ConcreteInlineFragment,
  ConcreteCondition,
  ConcreteDirective,
} from 'react-relay';

const {
  GraphQLEnumType,
  GraphQLInputType,
  GraphQLInputObjectType,
  GraphQLInterfaceType,
  GraphQLList,
  GraphQLNonNull,
  GraphQLObjectType,
  GraphQLScalarType,
  GraphQLType,
  GraphQLUnionType,
  GraphQLSchema,
} = require('graphql');

import type {ScalarTypeMapping} from 'relay-compiler/lib/RelayFlowTypeTransformers';

const babelGenerator = require('babel-generator').default;

const {isAbstractType} = SchemaUtils;

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
  nodes: Map<String, Root | Fragment>,
  useNulls?: boolean,
|};

function generate(
  node: Root | Fragment,
  options: Options,
): {core: string, supporting: string, implicits: string, objectParent: string} {

  // const code = babelGenerator(ast).code;
  // console.log(options.nodes);
  const newCT = new ClassTracker(options.nodes, options.useNulls || false);
  try {
    const ast = IRVisitor.visit(
      node,
      createVisitor(newCT)
    )
    const code = newCT.out();
    return code;
  } catch(e) {
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
  name?: ?string,
  members: Array<Member>,
  extendsC: Array<string>,
  open: boolean,
  linkedField: boolean,
  spreadFrags: Array<string>,
  isClass?: boolean,
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
  _nodes: Map<string, ConcreteRoot | ConcreteFragment>;
  _useNulls: boolean;

  constructor(nodes: Map<string, ConcreteRoot | ConcreteFragment>, useNulls: boolean) {
    this.classes = new Map();
    this.topClasses = new Map();
    this.fields = [];
    this.spreads = [];
    this._nodes = nodes;
    this.implicits = [];
    this.topLevelTypeParams = [];
    this.isQuery = false;
    this.isMutation = false;
    this._useNulls = useNulls;
  }

  newClassName(cn: string, n: ?number): string {
    const name = n ? `${cn}_${n}` :cn;
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

  newQueryTypeParam(input: string, output: string) {
    this.topLevelTypeParams.push({input, output});
  }

  newClass(cn: string, members: Array<Member>, extendsC: Array<string>, linkedField: boolean, spreadFrags: Array<string>): string {
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
      }
      this.topClasses.set(name, cls);
    }
    return name;
  }

  handleInline(node: ConcreteInlineFragment) {
    //$FlowFixMe
    const spreadFrags: Array<ConcreteFragmentSpread> = node.selections.filter(s => s.kind === "FragmentSpread");
    const selectionMembers = node.selections.filter(s => s.kind !== "FragmentSpread");
  }

  flattenMembers(members: Array<Member>, spreads: Array<[string, Array<Member>]>): Map<string, Array<Member>> {
    const m : Map<string, Array<Member>> = new Map();
    members.forEach(s => {
      const mem = m.get(s.name)
      m.set(s.name, mem ? mem.concat(s) : [s]);
    });

    for (const [key, value] of spreads) {
      value.forEach(s => {
        const mem = m.get(s.name)
        m.set(s.name, mem ? mem.concat(s) : [s]);
      });
    }
    return m;
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
    if (node.parentTpe) /* $FlowFixMe */
      return node.parentTpe.slice(1).join("");
    else /* $FlowFixMe */
      return node.parentTpe.join("");
  }

  getNodeName(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment): string {
    // TODO: Should I change this?
    const suffix = node.operation === "query" ? "" : "";
    // $FlowFixMe
    const nameAlias = (node.alias || node.name) + suffix;
    return nameAlias;
  }

  getNewTpe(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment): string {
    const nameAlias = this.getNodeName(node);
    if (node.kind == "LinkedField") {
      if (node.parentTpe) { /* $FlowFixMe */
        return node.parentTpe.slice(1).join("") + titleCase(nameAlias);
      }
    }
    return titleCase(nameAlias);
  }

  getNewTpeMember(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment): string {
    const nameAlias = this.getNodeName(node);
    if (node.kind == "LinkedField" || node.kind === "InlineFragment") {
      if (node.parentTpe) {
        if (node.parentTpe.length == 1) { /* $FlowFixMe */
          return node.parentTpe.join("") + titleCase(nameAlias)
        } else { /* $FlowFixMe */
          return node.parentTpe.slice(1).join("") + titleCase(nameAlias);
        }
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
    const localMembers: Array<Member> = selectionMembers.map(foo => this.popMember());
    const newTpe = this.newClass(newClassName, localMembers, [], true, []);
    const parentCls = this.getNewTpeParent(node);

    listOfSpreads.forEach(([k, {members}]) => {
      this.newImplicit(newClassName, k, k, false);
    });

    /* $FlowFixMe */
    this.newImplicit(parentCls, newTpe, `${node.typeCondition.toString()}`, true);
    node.selections;
  }

  /**
   * Handle adding a parent type to special classes like connections or edges or nodes.
   *
   * @param {ConcreteField} node The node in question
   * @param {Array<Member>} localMembers The list of members for this type
   * @param {string} className the name of the current class
   * @param {boolean} root is it a root
   */
  specialClassExtends(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment,
      localMembers: Array<Member>, className: string, root: boolean): Array<string> {
    if (root) return [];

    // $FlowFixMe
    const typeString = node.type instanceof GraphQLNonNull ? node.type.ofType.toString() : node.type.toString();

    if (className.endsWith("Edges")) {
      const nodeMem = localMembers.find(s => s.name == "node")
      if (nodeMem) {
        const tpe = this.makeTypeFromMember(className, nodeMem)
        return [`_root_.relay.runtime.Edge[${tpe}]`];
      }
      return [];
    }

    if (typeString.endsWith("Connection")) {
      const edgesMem = localMembers.find(s => s.name == "edges")
      if (edgesMem) {
        //
        const newEdge = Object.assign({}, edgesMem)
        newEdge.tpe = newEdge.tpe.map(t => {
          return {
            ...t,
            mods: []
          }
        })
        const tpe = this.makeTypeFromMember(className, newEdge);
        return [`_root_.relay.runtime.Connection[${tpe}]`];
      }
      return [];
    }

    return [];
  }

  /**
   * TODO: This needs to severly be cleaned up, wayyyy too much going on.
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
    let localMembers: Array<Member> = selectionMembers.map(foo => this.popMember());

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

    listOfSpreads.forEach(([k, {members}]) => {
      this.newImplicit(newClassName, k, k, false);
    });

    // flattenArray(listOfSpreads.map(s => s[1].extendCls));
    const fieldExtend: Array<string> = this.specialClassExtends(node, localMembers, newClassName, root);

    const newTpe = this.newClass(newClassName, localMembers, fieldExtend, linked, spreadParentsFrags);
    /* $FlowFixMe */
    const newNewTpe = this.transformScalarType(node.type , newTpe);
    let comments: Array<string> = [];

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

  handleQuery(node: ConcreteRoot) {
    const clsName = node.name + "Input";
    const newClasses : Array<Cls> = [];
    if (node.operation === 'query') {
      const members: Array<Member> = node.argumentDefinitions.map(({name, type, defaultValue}) => {
        const tpe = this.transformInputType(type, newClasses);
        return {name, tpe, comments: []};
      });
      this.newClass(clsName, members, [], false, []);
      this.newQueryTypeParam(clsName, node.name);
      this.isQuery = true;
    } else if (node.operation === 'mutation') {
      const members: Array<Member> = node.argumentDefinitions.map(({name, type, defaultValue}) => {
        const tpe = this.transformInputType(type, newClasses);
        return {name, tpe, comments: []};
      });
      this.newClass(clsName, members, [], false, []);
      this.newQueryTypeParam(clsName, node.name);
      this.isMutation = true;
    }
    newClasses.forEach(({name, members, extendsC, linkedField, spreadFrags}) => {
      this.newClass(name || '', members, extendsC, linkedField, spreadFrags);
    });
    // console.log(node);
  }

  newSpread(n: ConcreteFragmentSpread) {
    // $FlowFixMe
    const tpe = this.getNewTpe(n);
    // $FlowFixMe
    const extendCls: Array<string> = (n.metadata && n.metadata.extends && [n.metadata.extends]) || [];

    const dm = this.getDirectMembersForFrag(n.name, tpe).map(s => {
      s.or = true;
      return s;
    });
    this.spreads.unshift([n.name, {
      members: dm,
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

  memberCopy(): Array<Member> {
    return [].concat(this.fields);
  }

  jsPrefix(): string {
    return "js";
  }

  transformNonNullableScalarType(
    type: GraphQLType,
    backupType: ?string
  ): Array<ATpe> {
    if (type instanceof GraphQLList) {
      return this.transformScalarType(type.ofType, backupType).map(s => {

        s.mods.push(ARRAY_MOD);
        return s;
      });
    } else if (
      type instanceof GraphQLObjectType ||
      type instanceof GraphQLUnionType ||
      type instanceof GraphQLInterfaceType
    ) {
      // throw new Error(`Could not convert from GraphQL type ${type.toString()}`);
      if (!backupType) {
        throw new Error(`Could not convert from GraphQL type ${type.toString()}, missing backup type`);
      }
      return [{name: backupType, mods: []}];
    } else if (type instanceof GraphQLScalarType) {
      return this.transformGraphQLScalarType(type, backupType);
    } else if (type instanceof GraphQLEnumType) {
      return [{name: "String", mods: []}];
    } else {
      throw new Error(`Could not convert from GraphQL type ${type.toString()}`);
    }
  }

  transformInputType(type: GraphQLInputType, classes: Array<Cls>): Array<ATpe> {
    if (type instanceof GraphQLNonNull) {
      return this.transformNonNullableInputType(type.ofType, classes);
    } else {
      return this.transformNonNullableInputType(type, classes).map(s => {
        s.mods.push(OPTIONAL_MOD)
        return s;
      });
    }
  }

  transformNonNullableInputType(type: GraphQLInputType, classes: Array<Cls>): Array<ATpe> {
    if (type instanceof GraphQLList) {
      return this.transformInputType(type.ofType, classes).map(s => {
        s.mods.push(ARRAY_MOD);
        return s;
      });
    } else if (type instanceof GraphQLScalarType) {
      return this.transformGraphQLScalarType(type);
    } else if (type instanceof GraphQLEnumType) {
      return [{name: "String", mods: []}];
    } else if (type instanceof GraphQLInputObjectType) {
      const fields = type.getFields();
      const props: Array<Member> = Object.keys(fields)
        .map(key => fields[key])
        .map(field => {
          const property = this.transformInputType(field.type, classes)
          return {name: field.name,
            tpe: property,
            comments: [],
          };
        });

      classes.push({
        name: type.toString(),
        members: props,
        open: false,
        extendsC: [],
        linkedField: false,
        spreadFrags: [],
      });
      return [{
        name: type.toString(),
        mods: [INPUT_MOD],
      }];
    } else {
      throw new Error(`Could not convert from GraphQL Input type ${type.toString()}`);
    }
  }

  transformGraphQLScalarType(type: GraphQLScalarType, backupType: ?string): Array<ATpe> {
    switch (type.name) {
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
        return [{name: `js.Any`, mods: []}];
    }
  }

  transformScalarType(
    type: GraphQLType,
    backupType: ?string,
  ): Array<ATpe> {
    if (type instanceof GraphQLNonNull) {
      return this.transformNonNullableScalarType(type.ofType, backupType).map(s => {
        s.mods.push(OPTIONAL_MOD);
        return s;
      });
    } else {
      // $FlowFixMe
      return this.transformNonNullableScalarType(type, backupType);
    }
  }

  // combineFields(m: Member, m2: Member): Member {
  //   invariant(m.name === m2.name, "Names need to match to combine.");
  //   const tpeMap: Map<string, Array<ATpe>> = new Map();
  //   [...m.tpe, ...m2.tpe].forEach(tpe => {

  //     const found = tpeMap.get(tpe.name)
  //     if (!found) {
  //       tpeMap.set(tpe.name, [tpe]);
  //     } else {
  //       if (!found.every(s => {
  //         return s.isArray == tpe.isArray && s.isOptional == tpe.isOptional && s.name === tpe.name;
  //       })) {
  //         found.push(tpe);
  //       }
  //     }
  //   });

  //   return {
  //     ...m,
  //     or: m.or || m2.or,
  //     name: m.name,
  //     tpe: flattenArray(Array.from(tpeMap.values())),
  //     comments: [...m.comments, ...m2.comments],
  //     scalar: m.scalar || m2.scalar,
  //   }
  // }

  getDirectMembersForFrag(name: string, backupType: ?string): Array<Member> {
    const node = this._nodes.get(name);
    // console.log(node);
    if (node) {
      let result =  flattenArray(node.selections.map(s => {
        if (s.kind == "FragmentSpread") {
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
            scalar: s.kind == "ScalarField" ? true : false,
          }];
        }
      }))
      return result;
    } else return [];
  }

  makeType(className: string, s: ATpe, cs?: Set<string>): string {
    let newTpeName = cs && cs.has(s.name) ? className + "." + s.name: s.name;
    s.mods.forEach(mod => {
      if (mod == ARRAY_MOD) {
        newTpeName = "js.Array[" + newTpeName + "]";
      } else if (mod == OPTIONAL_MOD) {
        if (this._useNulls) {
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

  makeTypeFromMember(className: string, m: Member, otherClasses?: Map<string, Cls>): string {
    const joinStr = m.or ? " | " : " with ";
    let tpe = "";
    if (otherClasses) {
      // Declared new classes.
      const cs = new Set(otherClasses.keys());
      tpe = m.tpe.map(s => {
        return this.makeType(className, s, cs);
      }).join(joinStr);
    } else {
      tpe = m.tpe.map(s => {
        return this.makeType(className, s);
      }).join(joinStr);
    }
    return tpe;
  }

  outTrait({name, members, extendsC, open, isClass}: Cls, otherClasses?: Map<string, Cls>): string {
    invariant(name, "Name needs to be set");
    const indent = otherClasses ? "" : "  ";

    const ex = extendsC.length > 0 ? ["with", extendsC.join(" with ")] : [];
    const cls = indent + [`trait`, name, "extends", "js.Object", ...ex , "{"].join(" ");

    const outMembers = Array.from(members.values()).filter(s => !!s).map(s => {
      const comment = s.comments.length == 0 ? [] : ["  /**", ...s.comments, "*/"];

      if (!name) {
        throw new Error(`Class is missing name, has the following members: ${members.toString()}`);
      }

      // Figure out if we've created the type, if so, add a prefix.
      const tpe = this.makeTypeFromMember(name, s, otherClasses);
      const commentOut = comment.length > 0 ? indent + comment.join(" ") + "\n" : "";
      return commentOut + [indent + "  val", s.name, ":", tpe].join(" ");
    }).join("\n");

    return [cls, outMembers, indent + "}"].join("\n");
  }

  /**
   * Group all the implicits, and create
   * @param {} impl
   */
  outImplicitsFrags(impl: Array<ImplDef>): string {

    var groups: {[string]: Array<ImplDef>} = {};
    impl.forEach((item) => {
       var list = groups[item.from];

       if(list){
          list.push(item);
       } else{
          groups[item.from] = [item];
       }
    });

    return Object.keys(groups).map(k => {

      const items = groups[k];
      const indent = "  ";
      const members = Array.from(items).map(({from, to, name}) => {
        return [
          `${indent}  def as${name}: Option[${to}] = {`,
          `${indent}    if (f.__typename == "${name}") Some(f.asInstanceOf[${to}])`,
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

    // Handle explicits implicits we've asked for.
    const text = impl.map(({from, to, name}) => {
      return `  implicit def ${from}2${to}(f: ${from}): ${to} = f.asInstanceOf[${to}]`;
    }).join("\n");
    return [text].join("\n");
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
    ].join("\n");


    if (this.isQuery || this.isMutation) {
      invariant(this.topLevelTypeParams.length <= 1, "Something went wrong and there are multiple input objects.");
    }

    const objectPrefix = this.isQuery ?
      "_root_.relay.graphql.QueryTaggedNode" : (this.isMutation ?
        "_root_.relay.graphql.MutationTaggedNode" : "");

    const objectParent = this.topLevelTypeParams.length == 1 ?
      `${objectPrefix}[${this.topLevelTypeParams[0].input}, ${this.topLevelTypeParams[0].output}]` :
      '_root_.relay.graphql.GenericGraphQLTaggedNode';

    return {
        core: Array.from(this.topClasses.entries()).map(s => {
            const cls = s[1];
            if (cls) {
              return this.outTrait(cls, this.classes);
            } else return "";
          }).join("\n"),
        supporting,
        implicits,
        objectParent
    };
  }
}

function createVisitor(ct: ClassTracker) {
  return {
    enter : {
      Root(node: ConcreteRoot) {
        const selections = node.selections.map(s => {
          return {
            ...s,
            parentTpe: [node.name]
          }
        })
        return {
          ...node,
          selections
        };
      },
      Fragment(node: ConcreteFragment) {
        const selections = node.selections.map(s => {
          return {
            ...s,
            parentTpe: [node.name]
          }
        })
        return {
          ...node,
          selections
        };
      },
      InlineFragment(node: ConcreteInlineFragment) {
        // $FlowFixMe
        const {parentTpe} = node;
        const ptpe = parentTpe || [];
        const selections = node.selections.map(s => {
          return {
            ...s,
            parentTpe: ptpe,
          }
        })
        return {
          ...node,
          selections
        };
      },
      ScalarField(node: ConcreteScalarField) {
        return node;
      },
      LinkedField(node: ConcreteLinkedField) {
        // $FlowFixMe
        const {parentTpe} = node;
        const ptpe = parentTpe || [];
        const selections = node.selections.map(s => {
          return {
            ...s,
            parentTpe: ptpe.concat([titleCase(node.name)]),
          }
        })
        return {
          ...node,
          selections
        };
      },
      FragmentSpread(node: ConcreteFragmentSpread) {
        return node;
      },
      Condition(node: ConcreteCondition) {
        return node;
      },
    },
    leave: {
      Root(node: ConcreteRoot) {
        ct.handleQuery(node);
        ct.closeField(node, false, true);
        // // $FlowFixMe
        // // node.selections.map(s => ct.handleSelections(s));
        // ct.handleSelections(node);
        return node;
      },
      Fragment(node: ConcreteFragment) {
        // console.log(node);
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
      ScalarField(node: ConcreteScalarField) {
        // console.log("ScalarField", node);
        // $FlowFixMe
        const tpe = ct.transformScalarType((node.type: GraphQLType));
        ct.newMember({name: node.alias || node.name, tpe, comments: []});
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
  SJSTransform.transform,
];

module.exports = {
  generate: Profiler.instrument(generate, 'RelayFlowGenerator.generate'),
  flowTransforms: FLOW_TRANSFORMS,
};