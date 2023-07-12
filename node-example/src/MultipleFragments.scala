package web.admin.cps

import relay.Hooks
import relay.compiler.graphqlGen
import slinky.core.{FunctionalComponent, FunctionalComponentName, KeyAddingStage}
import slinky.web.html._

object MultipleFragments {

  // Test to make sure that scala2gql can put multiple fragments into the same gql file.

  val fragmentA = graphqlGen("""
    fragment MultipleFragments_a on WordDefinition {
      id text image
    }
  """)

  // TODO: It is currently not possible to have multiple in the same gql file.
  //  We might have to see if we can remove the silly validation error and remove the custom getModuleName fn.

//  val fragmentA = raphqlGen("""
//    fragment MultipleFragments_b on WordDefinition {
//      id text image
//    }
//  """)
}
