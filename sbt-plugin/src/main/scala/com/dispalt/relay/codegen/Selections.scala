package com.dispalt.relay.codegen

import caliban.parsing.adt.Selection
import com.dispalt.relay.codegen.Fields.{isMetaField, isTypeName}

object Selections {

  def nonMetaFieldSelections(selections: List[Selection]): List[Selection.Field] =
    selections.collect { case field: Selection.Field if !isMetaField(field.name) => field }

  def inlineFragmentSelections(selections: List[Selection]): List[Selection.InlineFragment] =
    selections.collect { case inline: Selection.InlineFragment => inline }

  def hasTypeName(selections: List[Selection]): Boolean =
    selections.exists {
      case field: Selection.Field => isTypeName(field.name)
      case _                      => false
    }
}
