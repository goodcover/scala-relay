package example

import java.util.NoSuchElementException
import relay.graphql
import relay.gql.FragmentRef
import scala.scalajs.js.|

@graphql("""
    fragment Main_node on Node {
      __typename
      ... on Comment {
        feedback {
          ...Main_feedback
        }
      }
      # Order matters. There was a bug which only occurred when this came last.
      ... on Feedback {
        ...Main_feedback
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
    val node: relay.generated.Main_node = null
    val user: Option[FragmentRef[relay.generated.Main_feedback]] = node.asFeedback.map(_.toMain_feedback)
    val author: Option[FragmentRef[relay.generated.Main_feedback]] = node.asComment.map(_.feedback.get.toMain_feedback)
  }
}
