package com.goodcover.relay.build.codegen

import caliban.parsing.adt.Selection
import com.goodcover.relay.build.codegen.Fields.isTypeName

object Selections {

  def selectableFieldSelections(selections: List[Selection]): List[Selection.Field] =
    selections.collect { case field: Selection.Field if !isTypeName(field.name) => field }

  def inlineFragmentSelections(selections: List[Selection]): List[Selection.InlineFragment] =
    selections.collect { case inline: Selection.InlineFragment => inline }

  def hasTypeName(selections: List[Selection]): Boolean =
    selections.exists {
      case field: Selection.Field => isTypeName(field.name)
      case _                      => false
    }
}
