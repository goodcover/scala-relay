package sbtrelease

import sbt._
import sbt.Keys._
import sbt.Package.ManifestAttributes

import annotation.tailrec
import ReleasePlugin.autoImport._
import ReleaseKeys._
import sbtrelease.ReleaseStateTransformations.commitVersion

import scala.sys.process.{Process, ProcessLogger}

object ReleaseCustom {
  import Utilities._

  private def toProcessLogger(st: State): ProcessLogger = new ProcessLogger {
    override def err(s: => String): Unit = st.log.info(s)
    override def out(s: => String): Unit = st.log.info(s)
    override def buffer[T](f: => T): T   = st.log.buffer(f)
  }

  private def vcs(st: State): Vcs = {
    st.extract
      .get(releaseVcs)
      .getOrElse(sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))
  }

  lazy val doReleaseYarn = { st: State =>
    val v             = Project.extract(st)
    val pl            = toProcessLogger(st)
    val versionString = v.get(version)
    val bd            = v.get(baseDirectory)

    Process(s"yarn", bd / "node-compiler").!
    val cmd = Process(s"yarn publish --new-version $versionString --no-git-tag-version", bd / "node-compiler")
    cmd.!(pl)
    st
  }

  lazy val commitNextVersion = { st: State =>
    commitVersion(st, releaseNextCommitMessage)
  }

  def commitVersion: (State, TaskKey[String]) => State = { (st: State, commitMessage: TaskKey[String]) =>
    val log     = toProcessLogger(st)
    val file    = st.extract.get(releaseVersionFile).getCanonicalFile
    val base    = vcs(st).baseDir.getCanonicalFile
    val sign    = st.extract.get(releaseVcsSign)
    val signOff = st.extract.get(releaseVcsSignOff)
    val relativePath = IO
      .relativize(base, file)
      .getOrElse("Version file [%s] is outside of this VCS repository with base directory [%s]!" format (file, base))

    val packageJson = IO
      .relativize(base, st.extract.get(baseDirectory) / "node-compiler" / "package.json")
      .getOrElse("Version file [%s] is outside of this VCS repository with base directory [%s]!" format (file, base))

    vcs(st).add(relativePath) !! log
    vcs(st).add(packageJson) !! log
    val status = vcs(st).status.!!.trim

    val newState = if (status.nonEmpty) {
      val (state, msg) = st.extract.runTask(commitMessage, st)
      vcs(state).commit(msg, sign, signOff) ! log
      state
    } else {
      // nothing to commit. this happens if the version.sbt file hasn't changed.
      st
    }
    newState
  }
}
