package com.dispalt.relay

import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.{FieldDefinition, InputValueDefinition}
import com.dispalt.relay.GraphQLSchema.FieldTypeDefinition

package object codegen {

  private[codegen] type ArgLookup       = String => Option[InputValueDefinition]
  private[codegen] type FieldLookup     = String => Option[FieldDefinition]
  private[codegen] type FieldTypeLookup = String => Option[FieldTypeDefinition]
}
