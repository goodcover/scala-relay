/**
 *  @flow
 */

// const t = require('babel-types');
// import {
//   Program,
//   ExportNamedDeclaration, 
//   Node,
//   ObjectTypeAnnotation,
//   ObjectTypeProperty,
//   Identifier,
//   NullableTypeAnnotation,
//   GenericTypeAnnotation,
//   TypeParameterInstantiation,
// } from 'babel-types';

import type {
  ConcreteNode,
  ConcreteRoot,
  ConcreteFragment,
  ConcreteBatch,
  ConcreteSelection,
  ConcreteScalarField,
  ConcreteLinkedField
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
} = require('graphql');

const {
  SchemaUtils,
} = require('relay-compiler/lib/GraphQLCompilerPublic');

export class ScalaGen {
  root: ConcreteNode;
  _toPrint: Map<string, Array<string>>;
  _seenTypes: Set<string>;
  _curClass: Array<string>;
  constructor(ast: ConcreteNode) {
    this.root = ast;
    this._toPrint = new Map();
    this._seenTypes = new Set();
    this._curClass = [];
  }

  newClass(s: string) {
    this._curClass.unshift(s);
  }

  popClass(): string {
    return this._curClass.shift();
  }

  printClass() {
    this.print(this._curClass[0]);
  }

  print(s: string) {
    const cls = this._curClass[0];
    const v = this._toPrint.get(cls);
    if (v) {
      v.push(s);
    } else {
      this._toPrint.set(cls, [s]);
    }
  }

  ret() {
    this.print("\n");
  }

  openB(){ this.print("{\n"); }
  closeB(){ this.print("\n}"); }

  start(): void {
    this.run(this.root);
  }

  jsPrefix(): string {
    return "js";
  }

  anyType() {
    this.print(`${this.jsPrefix()}.Any`)
  }

  anyObjType() {
    this.print(`${this.jsPrefix()}.Dictionary[${this.jsPrefix()}.Any]`)
  }

  valDef(name: string, tpe: string) {
    this.print("val");
    this.print(name);
    this.print(":");
    this.print(tpe);
    this.ret();
  }

  traitDefOpen(ex: ?string) {
    this.print("trait");
    this.printClass();
    this.print("extends");
    this.print(`${this.jsPrefix()}.Object`);
    if (ex) {
      this.print("with");
      this.print(ex);
    }
    this.openB();
  }

  titleCase(s: string): string {
    return s.charAt(0).toUpperCase() + s.substr(1);
  }

  transformNonNullableScalarType(
    type: GraphQLType,
  ): string {
    if (type instanceof GraphQLList) {
      throw new Error(`Could not convert from GraphQL type ${type.toString()}`);
    } else if (
      type instanceof GraphQLObjectType ||
      type instanceof GraphQLUnionType ||
      type instanceof GraphQLInterfaceType
    ) {
      throw new Error(`Could not convert from GraphQL type ${type.toString()}`);
    } else if (type instanceof GraphQLScalarType) {
      return this.transformGraphQLScalarType(type);
    } else if (type instanceof GraphQLEnumType) {
      // TODO: String for now.
      return "String";
    } else {
      throw new Error(`Could not convert from GraphQL type ${type.toString()}`);
    }
  }

  transformNonNullableScalarType(type: GraphQLScalarType): string {
    switch (type.name) {
      case 'ID':
      case 'String':
      case 'Url':
        return "String";
      case 'Float':
      case 'Int':
        return "Double";
      case 'Boolean':
        return "Boolean";
      default:
        return `${this.jsPrefix()}.Any`;
    }
  }

  transformScalarType(
    type: GraphQLType,
  ) {
    if (type instanceof GraphQLNonNull) {
      return this.transformNonNullableScalarType(type.ofType);
    } else {
      // $FlowFixMe
      return this.transformNonNullableScalarType(type);
    }
  }
  
  runSelection(parent: ConcreteFragment | ConcreteRoot, a: ConcreteSelection) {
    switch (a.kind) {
      case "ScalarField":
        const csf: ConcreteScalarField = a;
        let tpe: string = "";
        if (csf.type) {
          let required = false;
          // $FlowFixMe
          tpe = this.transformScalarType((csf.type: GraphQLType));
        } else {
          tpe = `${this.jsPrefix()}.Any`;
        }
        this.valDef(csf.name, tpe);
        break;
      case "LinkedField":
        const clf: ConcreteLinkedField = a;
        const onlyClasses = clf.selections.every((s) => s.kind === "FragmentSpread");
        const onlyMembers = clf.selections.every((s) => s.kind === "ScalarField");
        if (onlyClasses) {
          // $FlowFixMe
          const clss: Array<string> = clf.selections.map((s) => (s.name: string));
          const tpe = clss.join(" with ");
          this.valDef(clf.name, tpe);
        } else {
          const newCls = parent.name + this.titleCase(clf.name);
          
          // Get all the mixed in Fragments to Inherit from.
          const parentTpe: Array<string> = clf
            .selections
            .filter((s) => s.kind === "FragmentSpread")
             // $FlowFixMe
            .map((s) => (s.name: string));

          const tpe = parentTpe.join(" with ");
          this.newClass(newCls);
          // create a new class here.
          this.traitDefOpen(tpe);
          clf.selections.filter((s) => s.kind !== "FragmentSpread").map((s) => this.runSelection(parent, s));
          this.closeB();
          this.popClass();
          this.valDef(clf.name, newCls);
        }
        break;

      case "FragmentSpread":
        console.error("Shouldn't encounter a FragmentSpread directly. ", a);
        break;
    }
  }

  run(a: ConcreteNode): void {
    switch (a.kind) {
      case 'Fragment':
        const p: ConcreteFragment = a;
        this.newClass(p.name);
        this.traitDefOpen();
        p.selections.map((s) => this.runSelection(a, s));
        this.closeB();

        break;
      case 'Root':
        const r: ConcreteRoot = a;
        this.newClass(r.name);
        this.traitDefOpen();
        r.selections.map((s) => this.runSelection(a, s));
        this.closeB();
      // case "ExportNamedDeclaration":
      //   const end: ExportNamedDeclaration = a;

      //   if (end.declaration.type == "TypeAlias") {
      //     this.print("trait");
      //   }

      //   this.run(end.declaration.id);
      //   this.print("extends");
      //   this.print(`${this.jsPrefix()}.Object`);
      //   this.openB();
      //   this.ret();
      //   this.run(end.declaration.right);
      //   this.closeB();
      //   break;

      // case "ObjectTypeAnnotation":
      //   const ota: ObjectTypeAnnotation = a;
      //   if (ota.properties.length == 0) {
      //     this.anyObjType();
      //   } else {
      //     ota.properties.map((s) => this.run(s))
      //   }
      //   break;

      // case "GenericTypeAnnotation":
      //   const gta: GenericTypeAnnotation = a;
      //   switch (gta.id.name) {
      //     case "$ReadOnlyArray":
      //       this.print(`${this.jsPrefix()}.Array[`);
      //       this.run(gta.typeParameters);
      //       this.print(']');
      //       break;
      //   };
      //   break;

      // case "ObjectTypeProperty":
      //   const otp: ObjectTypeProperty = a;
      //   this.print("val")
      //   this.run(otp.key);
      //   this.print(":");
      //   this.run(otp.value);
      //   this.ret();
      //   break;

      // case "NullableTypeAnnotation":
      //   const nta: NullableTypeAnnotation = a;
      //   this.run(nta.typeAnnotation)
      //   break;
        
      // case "Identifier":
      //   const identifier: Identifier = a;
      //   this.print(identifier.name)
      //   break;

      // case "StringTypeAnnotation":
      //   this.print("String");
      //   break;

      // case "TypeParameterInstantiation":
      //   const tpi: TypeParameterInstantiation = a;
      //   tpi.params.map((s) => this.run(s));
      //   break;
    }
  }

  out(): string {
    const arr: Array<string> = [];
    for (const v of this._toPrint.entries()) {
      arr.push(v[1].join(" "));
    }

    return arr.join("\n");
  }
}