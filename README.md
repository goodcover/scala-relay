
# Relay Modern Tools for Scala.js Folks

There are three parts to this.
  - Sbt plugin - `addSbtPlugin("com.goodcover.relay" % "sbt-relay-compiler" % "<version>")`
    which handles code generation sbt hooks
  - Scala.js runtime (plain scalajs) - `"com.goodcover.relay" %%% "relay-macro" % "<version>"` which 
    handles the runtime, which is very small (intentionally).
  - Slinky relay module - `"com.goodcover.relay" %%% "slinky-relay" % "<version>"`

### [Change log](./CHANGELOG.md)

## Dev Notes

 - call `release` to deploy everything, you'll need a GPG key, and Sonatype credentials handy
 - call `slinky-relay-ijext/updateIntelliJ`
