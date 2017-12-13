
# Relay Modern Tools for Scala.js Folks

There are three parts to this.
  - Node compiler - `scala-relay-compiler` [details](./node-compiler/)
  - Sbt plugin - `addSbtPlugin("com.dispalt.relay" % "sbt-relay-compiler" % "<version>")`
    which handles code generation sbt hooks
  - Scala.js runtime - `"com.dispalt.relay" %%% "relay-macro" % "<version>"` which 
    handles the runtime, which is very small.


## Changes

### 0.8.0
 - Mutations and Queries now are fully typed input and output
 - Published for npm as well.  I haven't run across a query that can't compile.
   however I don't use some of the advanced features.

### 0.6.8
 - published for sbt 1

## Notes

 - Manually publish for bintray as follow `publishSigned` from `sbt-relay-compiler` 
   then `bintrayRelease`
 - Sonatype releasing seems to not be automatic anymore.  you should be able to use
   `sonatypeReleaseAll` after `publishSigned`
