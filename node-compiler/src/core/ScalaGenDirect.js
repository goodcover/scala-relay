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
  exactObjectTypeAnnotation,
  exportType,
  lineComments,
  readOnlyArrayOfType,
  readOnlyObjectTypeProperty,
  stringLiteralTypeAnnotation
} = require('./ScalaBabelFactories');
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

const {ScalaGen} = require('./ScalaGen');

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
): string {
  // const ast = IRVisitor.visit(
  //   node,
  //   createVisitor(customScalars || {}, inputFieldWhiteList, nodes),
  // );

  // const code = babelGenerator(ast).code;
  const scala = new ScalaGen(node);
  const result = scala.out();
  const newCT = new ClassTracker(nodes);
  console.log("NEWCT");
  try {
    const ast = IRVisitor.visit(
      node,
      createVisitor2(newCT)
    )
    console.log(newCT.out());
    // console.log(JSON.stringify(ast, null, 2));
    // console.log(JSON.stringify(compiledNode, null, 2));
    // console.log(code);
    scala.start();
    
    // console.log(result);
  } catch(e) {
    console.error(e);
  }

  return result;
}

type Member = {
  name: string,
  tpe: Array<string>,
  comments: Array<string>,
  node?: ?ConcreteNode,
};

type Cls = {
  name?: ?string,
  members: Array<Member>,
  extendsC: Array<string>,
  open: boolean,
  linkedField: boolean,
  children?: ?Map<string, Cls>,
};

class ClassTracker {
  
  classes: Map<string, Cls>;
  passes: Array<string>;
  fields: Array<Member>;
  spreads: Array<string>;
  _nodes: Array<ConcreteRoot | ConcreteFragment>;
  _missingFrags: Array<string>;

  constructor(nodes: Array<ConcreteRoot | ConcreteFragment>) {
    this.classes = new Map();
    this.passes = [];
    this.fields = [];
    this.spreads = [];
    this._nodes = nodes;
    this._missingFrags = [];
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

  
  newClass(cn: string, members: Array<Member>, extendsC: Array<string>, linkedField: boolean): string {
    const name = this.newClassName(cn);
    this.classes.set(name, {
      name, 
      members,
      extendsC,
      linkedField,
      open: false,
    });
    this.passes.push(name);
    return name;
  }

  // TODO!
  mergeMembers(m: Member, m2: Member): Member {
    if (m.name !== m2.name) throw new Error('Trying to merge unrelated fields.');
    
    return m;
  }

  closeField(node: ConcreteLinkedField, linked: boolean) {
    //$FlowFixMe
    const spreadFrags: Array<ConcreteFragmentSpread> = node.selections.filter(s => s.kind === "FragmentSpread");
    const selectionMembers = node.selections.filter(s => s.kind !== "FragmentSpread");
    const fieldExtend = [];
    let newFields: Array<Member> = [];

    // We modify this in place.
    const localMembers: Array<Member> = selectionMembers.map(foo => this.popMember());
    const fieldName = node.alias ? node.alias : node.name;
    
    // Going to have to 
    if (spreadFrags.length > 0) {
      // Pop all the members off.
      spreadFrags.map((select) => {
        const dm = this.getDirectMembersForFrag(select.name, fieldName);
        const directMemberSet = new Set(dm.map(s => s.name));
        const intersect = new Set(localMembers.filter(x => directMemberSet.has(x.name)));
        if (intersect.size == 0) {
          fieldExtend.push(select.name);
        } else {
          // Edit the fields in place
          Array.from(intersect.values()).forEach(s => {
            // $FlowFixMe: proven above.
            const m: Member = localMembers.find(om => om.name === s.name);
            m.tpe = Array.from(new Set(m.tpe.concat(...dm.filter(exM => exM.name === s).map(s => s.tpe))).values());
          });
          // Append new fields from the intersected fields
          newFields = newFields.concat(dm.filter(s => !intersect.has(s.name)));
        }
      });
    }

    if (spreadFrags.length == node.selections.length && newFields.length == 0) {
      // Only FragmentSpreads and no conflicting members
      this.newMember({
        name: fieldName,
        tpe: fieldExtend,
        comments: [`Combined the fields on a spread: ${fieldExtend.toString()}`],
      });
    } else if (node.selections.length === selectionMembers.length) {
      // No spreads.
      const newTpe = this.newClass(fieldName, localMembers, [], linked);
      this.newMember({
        name: fieldName,
        tpe: [newTpe],
        comments: ["No spreads."],
      });
    } else if (newFields.length > 0) {
      // Spreads and members that are conflicting
      const newTpe = this.newClass(fieldName, localMembers.concat(newFields), fieldExtend, linked);
      this.newMember({
        name: fieldName,
        tpe: [newTpe],
        comments: ['New fields added, conflicts detected.'],
      });
    } else if (newFields.length == 0) {
      console.log("Else case in closing field, no conflicts, mixed");
      // Add field
      const newTpe = this.newClass(fieldName, localMembers, fieldExtend, linked);
      this.newMember({
        name: fieldName,
        tpe: [newTpe],
        comments: ['New fields added, inheriting but no conflicts'],
      });
    }
  }

  newSpread(ext: string) {
    this.spreads.unshift(ext);   
  }

  newMember(m: Member): void {
    this.fields.unshift(m);
  }

  popMember(): Member {
    return this.fields.shift();
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

  titleCase(s: string): string {
    return s.charAt(0).toUpperCase() + s.substr(1);
  }

  newPass() {
    this.passes = [];
  }

  out(): string {
    return this.passes.map(s => {
      const cls = this.classes.get(s);
      if (cls) {
        return this.outCls(cls);
      } else return ""
    }).join("\n");
  }

  transformNonNullableScalarType(
    type: GraphQLType,
    backupType: ?string,
  ): Array<string> {
    if (type instanceof GraphQLList) {
      // throw new Error(`Could not convert from GraphQL type ${type.toString()}`);
      return ["js.Array[", ...this.transformScalarType(type.ofType, backupType), "]"];
    } else if (
      type instanceof GraphQLObjectType ||
      type instanceof GraphQLUnionType ||
      type instanceof GraphQLInterfaceType
    ) {
      // throw new Error(`Could not convert from GraphQL type ${type.toString()}`);
      if (!backupType) {
        throw new Error(`Could not convert from GraphQL type ${type.toString()}, missing backup type`);
      }
      return [backupType];
    } else if (type instanceof GraphQLScalarType) {
      return this.transformGraphQLScalarType(type, backupType);
    } else if (type instanceof GraphQLEnumType) {
      // TODO: String for now.
      return ["String"];
    } else {
      throw new Error(`Could not convert from GraphQL type ${type.toString()}`);
    }
  }

  transformGraphQLScalarType(type: GraphQLScalarType, backupType: ?string): Array<string> {
    switch (type.name) {
      case 'ID':
      case 'String':
      case 'Url':
        return ["String"];
        
      case 'Float':
      case 'Int':
      case 'BigDecimal':
      case 'BigInt':
      case 'Long':
        return ["Double"];
      case 'Boolean':
        return ["Boolean"];
      default:
        if (backupType) {
          return [backupType];
        } 
        return [`js.Any`];
    }
  }

  transformScalarType(
    type: GraphQLType,
    backupType: ?string,
  ): Array<string> {
    if (type instanceof GraphQLNonNull) {
      return this.transformNonNullableScalarType(type.ofType, backupType);
    } else {
      // $FlowFixMe
      return this.transformNonNullableScalarType(type, backupType);
    }
  }

  getDirectMembersForFrag(name: string, backupType: ?string): Array<Member> {
    const node = this._nodes.find(n => n.kind === "Fragment" && n.name === name);
    if (node) {
      return node.selections.map(s => {
        return {
          // $FlowFixMe
          name: s.name,
          // $FlowFixMe
          tpe: this.transformScalarType(s.type, backupType).join(" "),
          comments: [`getDirectMembersForFrag child of ${name}`],
        };
      })
    } else return [];
  }

  outCls({name, members, extendsC, open}: Cls): string {
    // const newExtends = [];
    // for (const extended of extendsC) {
      
    //   const exMembers = this.getDirectMembersForFrag(extended);
    //   const set2 = new Set(exMembers.map(s => s.name));
    //   const intersect = new Set(Array.from(members.keys()).filter(x => set2.has(x)));
    //   if (intersect.size == 0) {
    //     newExtends.push(extended);
    //   } else {
    //     Array.from(intersect.values()).map((s) => {
    //       // $FlowFixMe: proven above.
    //       const m: Member = members.get(s)
          
    //       m.tpe = m.tpe.concat(...exMembers.filter(exM => exM.name === s).map(s => s.tpe));
    //     })
    //   }

    // }
    

    const ex = extendsC.length > 0 ? ["with", extendsC.join(" with ")] : [];
    const cls = ["trait", name, "extends", "js.Object", ...ex , "{"].join(" ");
    const m = Array.from(members.values()).map(s => {
      const comment = s.comments.length == 0 ? [] : ["  /**", ...s.comments, "*/", "\n"];
      return [comment.join(" "), "  val", s.name, ":", s.tpe].join(" ");
    }).join("\n");

    return [cls, m, "}"].join("\n");
  }
}

function createVisitor2(ct: ClassTracker) {
  return {
    leave: {
      Root(node: ConcreteRoot) {
        // console.log("Root", node);
        ct.closeField(node, true);
        // // $FlowFixMe
        // // node.selections.map(s => ct.handleSelections(s));
        // ct.handleSelections(node);
        return node;
      },
      Fragment(node: ConcreteFragment) {
        // console.log("Fragment", node);
        ct.closeField(node, true);
        // // $FlowFixMe
        // ct.handleSelections(node);
        return node;
      },
      InlineFragment(node: ConcreteInlineFragment) {
        // console.log("InlineFragment", node);
        return node;
      },
      ScalarField(node: ConcreteScalarField) {
        // console.log("ScalarField", node);
        // $FlowFixMe
        const tpe = [ct.transformScalarType((node.type: GraphQLType)).join(" ")];
        ct.newMember({name: node.name, tpe, comments: []});
        return node;
      },
      LinkedField(node: ConcreteLinkedField) {
        // console.log("LinkedField", node);
        ct.closeField(node, true);
        return node;
      },
      FragmentSpread(node: ConcreteFragmentSpread) {
        // console.log("FragmentSpread", node);
        ct.newSpread(node.name);
        return node;
      },
      Condition(node: ConcreteCondition) {
        // console.log("Condition", node);
        return node;
      },
    }
  }
}














// function makeProp(
//   {key, schemaName, value, conditional, nodeType, nodeSelections},
//   customScalars: ScalarTypeMapping,
//   concreteType,
// ) {
//   if (nodeType) {
//     value = transformScalarType(
//       nodeType,
//       customScalars,
//       selectionsToBabel([Array.from(nodeSelections.values())], customScalars),
//     );
//   }
//   if (schemaName === '__typename' && concreteType) {
//     value = stringLiteralTypeAnnotation(concreteType);
//   }
//   const typeProperty = readOnlyObjectTypeProperty(key, value);
//   if (conditional) {
//     typeProperty.optional = true;
//   }
//   return typeProperty;
// }

// const isTypenameSelection = selection => selection.schemaName === '__typename';
// const hasTypenameSelection = selections => selections.some(isTypenameSelection);
// const onlySelectsTypename = selections => selections.every(isTypenameSelection);

// function selectionsToBabel(selections, customScalars: ScalarTypeMapping) {
//   const baseFields = new Map();
//   const byConcreteType = {};

//   flattenArray(selections).forEach(selection => {
//     const {concreteType} = selection;
//     if (concreteType) {
//       byConcreteType[concreteType] = byConcreteType[concreteType] || [];
//       byConcreteType[concreteType].push(selection);
//     } else {
//       const previousSel = baseFields.get(selection.key);

//       baseFields.set(
//         selection.key,
//         previousSel ? mergeSelection(selection, previousSel) : selection,
//       );
//     }
//   });

//   const types = [];

//   if (
//     Object.keys(byConcreteType).length &&
//     onlySelectsTypename(Array.from(baseFields.values())) &&
//     (hasTypenameSelection(Array.from(baseFields.values())) ||
//       Object.keys(byConcreteType).every(type =>
//         hasTypenameSelection(byConcreteType[type]),
//       ))
//   ) {
//     for (const concreteType in byConcreteType) {
//       types.push(
//         exactObjectTypeAnnotation([
//           ...Array.from(baseFields.values()).map(selection =>
//             makeProp(selection, customScalars, concreteType),
//           ),
//           ...byConcreteType[concreteType].map(selection =>
//             makeProp(selection, customScalars, concreteType),
//           ),
//         ]),
//       );
//     }
//     // It might be some other type then the listed concrete types. Ideally, we
//     // would set the type to diff(string, set of listed concrete types), but
//     // this doesn't exist in Flow at the time.
//     const otherProp = readOnlyObjectTypeProperty(
//       '__typename',
//       stringLiteralTypeAnnotation('%other'),
//     );
//     otherProp.leadingComments = lineComments(
//       "This will never be '%other', but we need some",
//       'value in case none of the concrete values match.',
//     );
//     types.push(exactObjectTypeAnnotation([otherProp]));
//   } else {
//     let selectionMap = selectionsToMap(Array.from(baseFields.values()));
//     for (const concreteType in byConcreteType) {
//       selectionMap = mergeSelections(
//         selectionMap,
//         selectionsToMap(
//           byConcreteType[concreteType].map(sel => ({
//             ...sel,
//             conditional: true,
//           })),
//         ),
//       );
//     }
//     const selectionMapValues = Array.from(selectionMap.values()).map(
//       sel =>
//         isTypenameSelection(sel) && sel.concreteType
//           ? makeProp(
//               {...sel, conditional: false},
//               customScalars,
//               sel.concreteType,
//             )
//           : makeProp(sel, customScalars),
//     );
//     types.push(exactObjectTypeAnnotation(selectionMapValues));
//   }

//   if (types.length === 0) {
//     return exactObjectTypeAnnotation([]);
//   }

//   return types.length > 1 ? t.unionTypeAnnotation(types) : types[0];
// }

// function mergeSelection(a, b) {
//   if (!a) {
//     return {
//       ...b,
//       conditional: true,
//     };
//   }
//   return {
//     ...a,
//     nodeSelections: a.nodeSelections
//       ? mergeSelections(a.nodeSelections, b.nodeSelections)
//       : null,
//     conditional: a.conditional && b.conditional,
//   };
// }

// function mergeSelections(a, b) {
//   const merged = new Map();
//   for (const [key, value] of a.entries()) {
//     merged.set(key, value);
//   }
//   for (const [key, value] of b.entries()) {
//     merged.set(key, mergeSelection(a.get(key), value));
//   }
//   return merged;
// }

// function isPlural({directives}): boolean {
//   const relayDirective = directives.find(({name}) => name === 'relay');
//   return (
//     relayDirective != null &&
//     relayDirective.args.some(
//       ({name, value}) => name === 'plural' && value.value,
//     )
//   );
// }



// function createVisitor(
//   customScalars: ScalarTypeMapping,
//   inputFieldWhiteList: ?Array<string>,
//   nodes: Array<Root | Fragment>,
// ) {
//   return {
//     // enter: {
//     //   FragmentSpread(node) {
//     //     const fragmentNode = nodes.find(n => n.kind === 'Fragment' && n.name === node.name);
//     //     if (fragmentNode) {
//     //       const newObj = {
//     //         ...fragmentNode,
//     //         kind: 'InlineFragment', // Replace by an inline fragment
//     //         typeCondition: fragmentNode.type,
//     //       };
//     //       return newObj;
//     //     } else {
//     //       return {};
//     //     }
//     //   }
//     // },
//     leave: {
//       Root(node) {
//         const statements = [];
//         if (node.operation !== 'query') {
//           statements.push(
//             generateInputVariablesType(
//               node,
//               customScalars,
//               inputFieldWhiteList,
//             ),
//           );
//         }
//         statements.push(
//           exportType(
//             `${node.name}Response`,
//             selectionsToBabel(node.selections, customScalars),
//           ),
//         );
//         return t.program(statements);
//       },

//       Fragment(node) {
//         let selections = flattenArray(node.selections);
//         const numConecreteSelections = selections.filter(s => s.concreteType)
//           .length;
//         selections = selections.map(selection => {
//           if (
//             numConecreteSelections <= 1 &&
//             isTypenameSelection(selection) &&
//             !isAbstractType(node.type)
//           ) {
//             return [
//               {
//                 ...selection,
//                 concreteType: node.type.toString(),
//               },
//             ];
//           }
//           return [selection];
//         });
//         const baseType = selectionsToBabel(selections, customScalars);
//         const type = isPlural(node) ? readOnlyArrayOfType(baseType) : baseType;

//         return t.program([exportType(node.name, type)]);
//       },

//       InlineFragment(node) {
//         const typeCondition = node.typeCondition;

//         return flattenArray(node.selections).map(typeSelection => {
//           return isAbstractType(typeCondition)
//             ? {
//                 ...typeSelection,
//                 conditional: true,
//               }
//             : {
//                 ...typeSelection,
//                 concreteType: typeCondition.toString(),
//               };
//         });
//       },
//       Condition(node) {
//         return flattenArray(node.selections).map(selection => {
//           return {
//             ...selection,
//             conditional: true,
//           };
//         });
//       },
//       ScalarField(node) {
//         return [
//           {
//             key: node.alias || node.name,
//             schemaName: node.name,
//             value: transformScalarType(node.type, customScalars),
//           },
//         ];
//       },
//       LinkedField(node) {
//         return [
//           {
//             key: node.alias || node.name,
//             schemaName: node.name,
//             nodeType: node.type,
//             nodeSelections: selectionsToMap(flattenArray(node.selections)),
//           },
//         ];
//       },
//       FragmentSpread(node) {
//         return [];
//       },
//     },
//   };
// }



// function selectionsToMap(selections) {
//   const map = new Map();
//   selections.forEach(selection => {
//     const previousSel = map.get(selection.key);
//     map.set(
//       selection.key,
//       previousSel ? mergeSelection(previousSel, selection) : selection,
//     );
//   });
//   return map;
// }

// function flattenArray<T>(arrayOfArrays: Array<Array<T>>): Array<T> {
//   const result = [];
//   arrayOfArrays.forEach(array => result.push(...array));
//   return result;
// }

// function generateInputVariablesType(
//   node: Root,
//   customScalars: ScalarTypeMapping,
//   inputFieldWhiteList?: ?Array<string>,
// ) {
//   return exportType(
//     `${node.name}Variables`,
//     exactObjectTypeAnnotation(
//       node.argumentDefinitions.map(arg => {
//         const property = t.objectTypeProperty(
//           t.identifier(arg.name),
//           transformInputType(arg.type, customScalars, inputFieldWhiteList),
//         );
//         if (!(arg.type instanceof GraphQLNonNull)) {
//           property.optional = true;
//         }
//         return property;
//       }),
//     ),
//   );
// }

const FLOW_TRANSFORMS: Array<IRTransform> = [
  (ctx: CompilerContext) => FlattenTransform.transform(ctx, {}),
];

module.exports = {
  generate,
  flowTransforms: FLOW_TRANSFORMS,
};