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
} = require('relay-compiler/lib/GraphQLCompilerPublic');

const {
  transformScalarType,
  transformInputType,
} = require('./ScalaTypeTransformers');

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

/*
Better typing ideas here.

https://github.com/facebook/relay/issues/1918

*/

function generate(
  node: Root | Fragment,
  customScalars?: ?ScalarTypeMapping,
  inputFieldWhiteList?: ?Array<string>,
  schema: GraphQLSchema,
  nodes: Array<Root | Fragment>,
): {core: string, supporting: string, implicits: string} {

  // const code = babelGenerator(ast).code;
  const newCT = new ClassTracker(nodes);
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
  isArray?: boolean,
  isOptional?: boolean,
}

type Member = {
  name: string,
  tpe: Array<ATpe>,
  comments: Array<string>,
  node?: ?ConcreteNode,
  parentFrag?: ?string,
  scalar: boolean,
  or?: ?boolean,
};

type Cls = {
  name?: ?string,
  members: Array<Member>,
  extendsC: Array<string>,
  open: boolean,
  linkedField: boolean,
  spreadFrags: Array<string>,
};

type ImplDef = {
  from: string,
  to: string,
  name: string,
  inline: boolean,
}

class ClassTracker {

  classes: Map<string, Cls>;
  topClasses: Map<string, Cls>;
  implicits: Array<ImplDef>;
  fields: Array<Member>;
  spreads: Array<[string, ASpread]>;
  _nodes: Array<ConcreteRoot | ConcreteFragment>;

  constructor(nodes: Array<ConcreteRoot | ConcreteFragment>) {
    this.classes = new Map();
    this.topClasses = new Map();
    this.fields = [];
    this.spreads = [];
    this._nodes = nodes;
    this.implicits = [];
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

  getNewTpe(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment): string {
    if (node.kind == "LinkedField") {
      if (node.parentTpe) { /* $FlowFixMe */
        return node.parentTpe.slice(1).join("") + titleCase(node.name);
      }
    }
    return titleCase(node.name);
  }

  getNewTpeMember(node: ConcreteLinkedField | ConcreteRoot | ConcreteFragment): string {
    if (node.kind == "LinkedField" || node.kind === "InlineFragment") {
      if (node.parentTpe) {
        if (node.parentTpe.length == 1) { /* $FlowFixMe */
          return node.parentTpe.join("") + titleCase(node.name)
        } else { /* $FlowFixMe */
          return node.parentTpe.slice(1).join("") + titleCase(node.name);
        }
      }
    }
    return titleCase(node.name);
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


    if (inlineFrags.length > 0) {
      // if we don't have __typename in the set of selection members add it synthetically since the relay compiler will
      // anyways. $FlowFixMe
      if (!selectionMembers.find(f => f.name === "__typename"))
        localMembers.push({name: "__typename", tpe: [{name: "String"}], comments: [], scalar: true});
    }

    const listOfSpreads: Array<[string, ASpread]> = spreadFrags.map(_ => this.popSpread());
    const fieldExtend: Array<string> = [];// flattenArray(listOfSpreads.map(s => s[1].extendCls));

    // $FlowFixMe
    const fieldName: string = node.alias ? node.alias : node.name;
    const newClassName: string = this.getNewTpe(node);
    const scalar = false;
    const spreadParentsFrags: Array<string> =
      Array.from(new Set(flattenArray(listOfSpreads.map(s => s[1].members.filter(s => !!s.parentFrag).map(s => s.parentFrag)))));

    // TODO: Revisit this approach.
    // // Very simple case, we are sure it can combine.
    // if (spreadFrags.length == 1 && selectionMembers.length == 0) {
    //   // Only FragmentSpreads and no conflicting members
    //   const newTpe = this.transformScalarType(node.type, spreadFrags[0].name);
    //   return this.newMember({
    //     name: fieldName,
    //     tpe: newTpe,
    //     comments: [`Combined the fields on a spread: ${spreadFrags[0].name}`],
    //     parentFrag: "",
    //     scalar,
    //   });
    // }

    listOfSpreads.forEach(([k, {members}]) => {
      this.newImplicit(newClassName, k, k, false);
    });


    // TODO: Revisit if we shouldn't just use implicits all together.
    if (spreadFrags.length > 0) {
      // Combine all the children both spreads and
      const localSpreads = listOfSpreads.map(s => [s[0], s[1].members])
      const sumOfMembers: Map<string, Array<Member>> = this.flattenMembers(localMembers, localSpreads);

      //TODO: This is like a shitty version of fold.
      localMembers = Array.from(sumOfMembers.entries()).map(s => {
        if (s[1].length == 1)
          return s[1][0];
        let m = s[1][0];
        s[1].slice(1).forEach(ss => {
          ss.comments.push(`Combining fields, with or? "${ss.or}" `)
          m = this.combineFields(m, ss);
        });
        return m;
      });
    }

    const newTpe = this.newClass(newClassName, localMembers, fieldExtend, linked, spreadParentsFrags);
    const newNewTpe = this.transformScalarType(node.type, newTpe);
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
      scalar,
    });
  }

  handleQuery(n: ConcreteRoot) {
  }

  hasAndStripSjsWithDirective(n: ConcreteFragmentSpread): boolean {
    // $FlowFixMe
    const hasD = n.directives.filter(s => s.name === "sjs" && s.args[0] && s.args[0].name == 'with' && s.args[0].value.value);
    n.directives = n.directives ? n.directives.filter(s => s.name !== "sjs") : [];
    return hasD.length > 0;
  }

  newSpread(n: ConcreteFragmentSpread) {
    // $FlowFixMe
    const tpe = this.getNewTpe(n);
    /* $FlowFixMe */
    const hasD = n.metadata && n.metadata.with;

    const dm = this.getDirectMembersForFrag(n.name, tpe).map(s => {
      if (hasD) {
        s.or = false;
      } else {
        s.or = true;
      }
      return s;
    });
    this.spreads.unshift([n.name, {
      members: dm,
      extendCls: [],
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

  mergeMemberTypes(m: Member): void {
    this.fields.forEach(mm => {
      // $FlowFixMe
      if (mm.name === m.name && mm.node && m.node && mm.node.type === m.node.type) {
        mm.tpe = mm.tpe.concat(m.tpe)
      }
    });
  }

  jsPrefix(): string {
    return "js";
  }

  anyType(): string {
    return `${this.jsPrefix()}.Any`;
  }

  anyObjType(): string {
    return `${this.jsPrefix()}.Dictionary[${this.jsPrefix()}.Any]`;
  }

  transformNonNullableScalarType(
    type: GraphQLType,
    backupType: ?string,
  ): Array<ATpe> {
    if (type instanceof GraphQLList) {
      // throw new Error(`Could not convert from GraphQL type ${type.toString()}`);
      return this.transformScalarType(type.ofType, backupType).map(s => {

        s.isArray = true;
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
      return [{name: backupType}];
    } else if (type instanceof GraphQLScalarType) {
      return this.transformGraphQLScalarType(type, backupType);
    } else if (type instanceof GraphQLEnumType) {
      // TODO: String for now.
      return [{name: "String"}];
    } else {
      throw new Error(`Could not convert from GraphQL type ${type.toString()}`);
    }
  }

  transformGraphQLScalarType(type: GraphQLScalarType, backupType: ?string): Array<ATpe> {
    switch (type.name) {
      case 'ID':
      case 'String':
      case 'Url':
        return [{name: "String"}];

      case 'Float':
      case 'Int':
      case 'BigDecimal':
      case 'BigInt':
      case 'Long':
        return [{name: "Double"}];
      case 'Boolean':
        return [{name: "Boolean"}];
      default:
        if (backupType) {
          return [{name: backupType}];
        }
        return [{name: `js.Any`}];
    }
  }

  transformScalarType(
    type: GraphQLType,
    backupType: ?string,
  ): Array<ATpe> {
    if (type instanceof GraphQLNonNull) {
      return this.transformNonNullableScalarType(type.ofType, backupType);
    } else {
      // $FlowFixMe
      return this.transformNonNullableScalarType(type, backupType);
    }
  }

  combineFields(m: Member, m2: Member): Member {
    invariant(m.name === m2.name, "Names need to match to combine.");
    const tpeMap: Map<string, Array<ATpe>> = new Map();
    [...m.tpe, ...m2.tpe].forEach(tpe => {

      const found = tpeMap.get(tpe.name)
      if (!found) {
        tpeMap.set(tpe.name, [tpe]);
      } else {
        if (!found.every(s => {
          return s.isArray == tpe.isArray && s.isOptional == tpe.isOptional && s.name === tpe.name;
        })) {
          found.push(tpe);
        }
      }
    });

    return {
      ...m,
      or: m.or || m2.or,
      name: m.name,
      tpe: flattenArray(Array.from(tpeMap.values())),
      comments: [...m.comments, ...m2.comments],
      scalar: m.scalar || m2.scalar,
    }
  }

  getDirectMembersForFrag(name: string, backupType: ?string): Array<Member> {
    const node = this._nodes.find(n => n.kind === "Fragment" && n.name === name);
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

  outCls({name, members, extendsC, open}: Cls, otherClasses?: ?Map<string, Cls>): string {
    invariant(name, "Name needs to be set");

    const ex = extendsC.length > 0 ? ["with", extendsC.join(" with ")] : [];
    const cls = ["trait", name, "extends", "js.Object", ...ex , "{"].join(" ");

    const m = Array.from(members.values()).filter(s => !!s).map(s => {
      const comment = s.comments.length == 0 ? [] : ["  /**", ...s.comments, "*/", "\n"];

      // Figure out if we've created the type, if so, add a prefix.
      let tpe = "";
      const joinStr = s.or ? " | " : " with ";
      if (otherClasses) {
        // Declared new classes.
        const cs = new Set(otherClasses.keys());
        tpe = s.tpe.map(s => {

          let newTpeName = cs.has(s.name) ? name + "." + s.name: s.name;
          if (s.isArray) {
            newTpeName = "js.Array[" + newTpeName + "]";
          }

          return newTpeName;
        }).join(joinStr);
      } else {
        tpe = s.tpe.map(s => {
          let newTpeName = s.name;
          if (s.isArray) {
            newTpeName = "js.Array[" + newTpeName + "]";
          }

          return newTpeName;
        }).join(joinStr);
      }

      return [comment.join(" "), "  val", s.name, ":", tpe].join(" ");
    }).join("\n");

    return [cls, m, "}"].join("\n");
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

      const items = groups[k]
      const members = Array.from(items).map(({from, to, name}) => {
        return [`  def as${name}: Option[${to}] = {`,
          `    if (f.__typename == "${name}") Some(f.asInstanceOf[${to}])`,
          `    else None`,
          '  }'
        ].join("\n  ");
      });
      return [`implicit class ${k}_Ops(f: ${k}) {`, ...members, `}`].join("\n  ");
    }).join("\n  ");

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

    // const oc = otherClasses && Array.from(otherClasses.values());
    // let extraImplicits = ""
    // if (oc) {
    //   oc.forEach(({spreadFrags, name}) => {
    //     spreadFrags.forEach(frag => {
    //       const i: ImplDef = {from: name, to: frag, name: frag, inline: false};
    //       impl.push(i);
    //     });
    //   });
    // }


    // Handle explicits implicits we've asked for.
    const text = impl.map(({from, to, name}) => {
      return `  implicit def ${from}2${to}(f: ${from}): ${to} = f.asInstanceOf[${to}]`;
    }).join("\n");

    // TODO: I am going to leave this in for now, its a weird concept though.
    // let implExtra = ""
    // const oc = otherClasses && Array.from(otherClasses.values()) || [];
    // if (oc) {
    //   implExtra = topClasses.map((tc) => {
    //     const tcName = tc.name;
    //     invariant(tcName, "top class missing name");

    //     return oc.map(({name, spreadFrags}) => {
    //       const ocName = name;
    //       invariant(ocName, "other class missing name");

    //       return spreadFrags.map(frag => {
    //         const rt = `relay.generated.lifted.Lifted${ocName}[${frag}]`;
    //         return [
    //           `  implicit def ${tcName}2${frag}(f: ${tcName})`, ':', rt,
    //           '=', `f.asInstanceOf[${rt}]`,
    //         ].join(" ");
    //       }).join("\n  ");
    //     }).join("\n  ");
    //   }).join("\n  ");
    // }
    return [text].join("\n");
  }

  out(): { supporting: string, core: string, implicits: string } {
    invariant(this.topClasses.size === 1, `there should be 1 "topClasses", got `);

    const supporting = Array.from(this.classes.entries())
      .map(s => s[1])
      .map(cls => {
        return this.outCls(cls);
      })
      .join("\n  ");

    const implicits = [
      this.outImplicitsFrags(this.implicits.filter(s => s.inline)),
      this.outImplicitsConversion(this.implicits.filter(s => !s.inline), this.classes, Array.from(this.topClasses.values())),
    ].join("\n  ");

    return {
        core: Array.from(this.topClasses.entries()).map(s => {
            const cls = s[1];
            if (cls) {
              return this.outCls(cls, this.classes);
            } else return "";
          }).join("\n"),
        supporting,
        implicits
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
        ct.newMember({name: node.name, tpe, comments: [], scalar: true});
        return node;
      },
      LinkedField(node: ConcreteLinkedField) {
        // console.log("LinkedField", node);
        ct.closeField(node, true, false);
        return node;
      },
      FragmentSpread(node: ConcreteFragmentSpread) {
        ct.newSpread(node);
        // console.log("FragmentSpread", node);
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
  (ctx: CompilerContext) => FlattenTransform.transform(ctx, {}),
  (ctx: CompilerContext) => SJSTransform.transform(ctx),
];

module.exports = {
  generate,
  flowTransforms: FLOW_TRANSFORMS,
};