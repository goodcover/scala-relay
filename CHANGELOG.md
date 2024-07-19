## Changes

### 0.46.0

- Allow `null` or `js.undefined` for optional input fields.

### 0.45.0

- Use shared input types.

### 0.44.0

- Remove unwrapping of mutation inputs.

### 0.43.0

- Rename organization and package to `com.goodcover.relay` and standardize artifact naming.

### 0.42.0

- Replace the relay-compiler plugin with a sbt based generator in preparation for supporting newer relay versions that
  do not support language plugins.

### 0.40.0

- Drop 2.12

### 0.22.8

- Support scala2.13

### 0.22.5

- Support relay refetch through a HasRefetch trait that gets mixed in.

### 0.22.4

- Fixed timestamps if you generate to resources directory it won't cause things to reload with
  artifactOutput set.

### 0.22.3

- Added a really rough slinky module, with the idea it handles some of the gluing by overloading the annotation
  from slinky core.
- Added an even rougher version of the intellij extension.

### 0.20.1

- Simplified the js code considerably by using the language hooks within the `relay-compiler`.
  There is a couple changes that make this a non-straightforward migration
    - Naming is more important now, some things accepted might not work now.
    - `@scalajs` directive is needed schema-side,
- Upgraded to relay 2.0
- Upgraded to use scalajs-bundler in the generate code step, which makes the whole process much simpler from
  an install standpoint.
- Supports persisted queries, you have to wire everything up yourself though.

### 0.11.0

- Upgrade relay to `1.6.2`, most of the changes seem import related
- Generate a `def newInput(args...): MutationInputType = ...` for all
  mutations, making it a little easier.

### 0.10.0

- Add ability to mix in traits with scalarFields so if value was
  a string `{ value @scalajs(extends:"Foo") }`
  The trait would have a member `val value: String with Foo`

### 0.9.4

- Added support for `@scalajs(useNulls: boolean)` that gives you finer grained access to
  using `A | Null`

### 0.9.2

- Added support to support `.gql` files in any directory from `baseDirectory.value` so while
  it looks over more files it can support alternate locations now.

### 0.9.0

- Relay 1.5.0 compiler support, nothing extra came of that minus some code changes.
- Add option for `--useNulls` that specifies using a union with Null type for optional fields.
- Add option for `--verbose` to increase verbosity
- Make `@scalajs(extends: string)` work for more cases (inline frags, linked fields, and frag defs)

### 0.8.2

- Actually works against 1.4.1 with everything I can test.

### 0.8.0

- Mutations and Queries now are fully typed input and output
- Published for npm as well. I haven't run across a query that can't compile.
  however I don't use some of the advanced features.

### 0.6.8

- published for sbt 1
