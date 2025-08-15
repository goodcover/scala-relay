package com.goodcover.relay.build

import caliban.parsing.adt.Definition.TypeSystemDefinition.TypeDefinition.{FieldDefinition, InputValueDefinition}
import com.goodcover.relay.build.GraphQLSchema.FieldTypeDefinition

package object codegen {

  private[codegen] type ArgLookup       = String => Option[InputValueDefinition]
  private[codegen] type FieldLookup     = String => Option[FieldDefinition]
  private[codegen] type FieldTypeLookup = String => Option[FieldTypeDefinition]

}
