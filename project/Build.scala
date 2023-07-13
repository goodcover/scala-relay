import sbt.ScriptedPlugin.autoImport.scripted
import sbt._

object Build extends AutoPlugin {

  override def requires: Plugins = ScriptedPlugin

  override def trigger = allRequirements

  object autoImport {

    lazy val scriptedAll = taskKey[Unit]("Runs all scripted tests")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    scriptedAll := scripted.toTask("").value
  )
}
