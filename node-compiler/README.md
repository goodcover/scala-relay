
## scala-relay-compiler

The purpose of this project is to generate Scala (sjs) bindings for the
`relay-compiler`.  Typically the `relay-compiler` generates `flow` bindings
along with the compiled queries.  This project replaces that generation and
outputs `js.native` traits instead.


## Example

```scala
$ ./bin/scala-relay-compiler.js --src example/src/ --schema example/schema.graphql --out example/out/
```

## Features
 - Handles names elegantly by scoping them to the companion object.
 - Handles first layer of spreading, right now we spit out `js.|` to
   join disjoint fields, even though in fact they are not disjoint,
   they are a union, however, this requires a fix later down the line.
 - `@sjs(with:Boolean!)` basically this allows us to control from a fragment spread level
   whether to combine the fields as an `js.|` or using ` with `.  With has more compile
   time constraints where ` js.| ` is easier to generate.

## TODO
 - [ ] Med: Fix `InlineFragments` so they work properly.  Right now we just really
 don't handle them.
 - [ ] Big: Fix spreading so it goes recursively?  Right now spreading is
 difficult because the types that are mixed in couple potentially conflict
   - [ ] Med: Make it work recursively
   - [ ] Med: Generate the necessary class depth.
   - [ ] Big: How to handle traversing since the order of the frags are unable to
    to be changed.
  - [ ] Med: Handle connections and edges with a superclass.
