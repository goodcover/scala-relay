# scala-relay

## Relay Modern Tools for Scala.js Folks

There are three parts to this:

- Sbt plugin - handles code generation sbt hooks (`relayConvert` and `relayCompile`).
 
  ```sbt
  addSbtPlugin("com.goodcover" % "sbt-scala-relay" % "<version>")
  ```
 
- Scala.js runtime (plain scalajs) - handles the runtime, which is very small (intentionally).

  ```sbt
  libraryDependencies += "com.goodcover.relay" %%% "scala-relay-macros" % "<version>"
  ```
 
### [Change Log](./CHANGELOG.md)

### [Contributing Guide](./CONTRIBUTING.md)
