package com.goodcover.relay.build

import caliban.parsing.adt.Definition.ExecutableDefinition.{FragmentDefinition, OperationDefinition}
import caliban.parsing.adt.OperationType

import scala.annotation.tailrec

object GraphQLText {

  // TODO: Use fastparse and caliban.parsing.parsers.Parsers to do better here.
  //  The current implementation is not very accurate but seems good enough for now.
  //  See https://spec.graphql.org/October2021.

  def splitComment(s: String): (String, String) = {
    val i = s.indexOf('#')
    if (i >= 0) s.splitAt(i) else (s, "")
  }

  def countSelectionSetOpens(s: String): Int =
    s.count(_ == '{')

  def countSelectionSetCloses(s: String): Int =
    s.count(_ == '}')

  @tailrec
  def countSelectionSetDiff(s: String, hasComments: Boolean = true): Int =
    if (hasComments) {
      val (noComments, _) = splitComment(s)
      countSelectionSetDiff(noComments, hasComments = false)
    } else {
      countSelectionSetOpens(s) - countSelectionSetCloses(s)
    }

  def appendOperationText(documentText: String, operation: OperationDefinition)(append: String => Unit): Unit =
    appendSelectionText(documentText, startOfOperation(_, operation))(append)

  def appendFragmentText(documentText: String, fragment: FragmentDefinition)(append: String => Unit): Unit =
    appendSelectionText(documentText, startOfFragment(_, fragment))(append)

  def appendSelectionText(documentText: String, startOfText: String => Boolean)(append: String => Unit): Unit = {
    val lines = documentText.linesWithSeparators

    @tailrec
    def loop(foundStart: Boolean, opens: Int): Unit = {
      if (lines.hasNext) {
        val line = lines.next()
        if (foundStart) {
          append(line)
          val diff      = countSelectionSetDiff(line)
          val nextOpens = opens + diff
          if (nextOpens > 0) {
            loop(foundStart, nextOpens)
          }
        } else {
          // TODO: This assumes that the query starts on a single line which is not true.
          // TODO: This also looses any preceding comments.
          if (startOfText(line)) {
            append(line)
            val (nonComment, _) = splitComment(line)
            val opens           = countSelectionSetOpens(nonComment)
            val closes          = countSelectionSetCloses(nonComment)
            if (opens > 0 && opens == closes) {
              append(line)
            } else {
              loop(foundStart = true, opens - closes)
            }
          } else {
            loop(foundStart, opens)
          }
        }
      }
    }

    loop(foundStart = false, 0)
  }

  def startOfOperation(s: String, operation: OperationDefinition): Boolean = {
    val operationType = operation.operationType match {
      case OperationType.Query        => "query"
      case OperationType.Mutation     => "mutation"
      case OperationType.Subscription => "subscription"
    }
    operation.name match {
      case Some(name) => startOfNamed(s, operationType, name)
      // TODO: This ought to use matcher.find().
      case None => s.matches(s"""(?s).*\\{.*""")
    }
  }

  def startOfFragment(s: String, fragment: FragmentDefinition): Boolean =
    startOfNamed(s, "fragment", fragment.name)

  def startOfNamed(s: String, kind: String, name: String): Boolean =
    // TODO: This ought to use matcher.find().
    s.matches(s"""(?s).*(?:^|[\\n\\t ])+$kind[\\n\\t ]*$name(?![a-zA-Z0-9]).*""")
}
