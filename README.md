
# Relay Modern Tools for Scala.js Folks

There are three parts to this.
  - Relay Compiler Language for SJS - `relay-compiler-language-scalajs` [details](./node-compiler/)
  - Sbt plugin - `addSbtPlugin("com.dispalt.relay" % "sbt-relay-compiler" % "<version>")`
    which handles code generation sbt hooks
  - Scala.js runtime (plain scalajs) - `"com.dispalt.relay" %%% "relay-macro" % "<version>"` which 
    handles the runtime, which is very small (intentionally).
  - Slinky relay module - `"com.dispalt.relay" %%% "slinky-relay" % "<version>"`

### [Change log](./CHANGELOG.md)

## Dev Notes

 - call `release` to deploy everything, you'll need a GPG key, and Sonatype credentials handy
 - call `slinky-relay-ijext/updateIntellij`
