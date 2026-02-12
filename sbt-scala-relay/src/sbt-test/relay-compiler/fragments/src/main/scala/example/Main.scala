package example

import java.util.NoSuchElementException
import relay.generated._
import com.goodcover.relay.graphql
import com.goodcover.relay.FragmentRef
import scala.scalajs.js.|

// The order here matters.
// There is a bug where if an object spread occurs with a field that has the same name as the type name within the
// type condition of a fragment spread.
@graphql("""
    fragment Main_node on Node {
      __typename
      ... on Comment {
        # An alias works around the bug.
        foo: feedback {
          ...Main_feedback
        }
      }
      ... on Feedback {
        ...Main_feedback
      }
    }
  """)
@graphql("""
    fragment Main_node2 on Node {
      __typename
      # Changing the order also fixes the bug.
      ... on Feedback {
        ...Main_feedback
      }
      ... on Comment {
        feedback {
          ...Main_feedback
        }
      }
    }
  """)
@graphql("""
    fragment Main_feedback on Feedback {
      body {
        text
      }
    }
  """)
object frag

object Main extends App {

  implicit class OrNullOps[A](val orNull: A | Null) {
    def get: A = if (orNull == null) throw new NoSuchElementException("null.get") else orNull.asInstanceOf[A]
  }

  def main(args: List[String]): Unit = {
    locally {
      val node: Main_node                            = null
      val user: Option[FragmentRef[Main_feedback]]   = node.asFeedback.map(_.toMain_feedback)
      val author: Option[FragmentRef[Main_feedback]] = node.asComment.map(_.foo.get.toMain_feedback)
    }
    locally {
      val node: Main_node2                           = null
      val user: Option[FragmentRef[Main_feedback]]   = node.asFeedback.map(_.toMain_feedback)
      val author: Option[FragmentRef[Main_feedback]] = node.asComment.map(_.feedback.get.toMain_feedback)
    }
  }
}
