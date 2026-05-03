# Repository Guidelines

## Project Structure & Module Organization

This is an sbt multi-module Scala project for Relay tooling. Core runtime and macros live in `scala-relay-core/` and `scala-relay-macros/`; build-time GraphQL extraction and code generation live in `scala-relay-build/`; sbt and Mill integrations live in `sbt-scala-relay/` and `mill-scala-relay/`; IntelliJ macro support lives in `scala-relay-ijext/`. Shared sbt configuration is in `build.sbt` and `project/`. Scala sources use `src/main/scala`, with version-specific directories such as `src/main/scala-2.12`, `src/main/scala-2.13`, `src/main/scala-2`, and `src/main/scala-3`. Tests are under `src/test`, and sbt scripted integration fixtures are under `sbt-scala-relay/src/sbt-test/`.

## Build, Test, and Development Commands

- `sbt +test` runs cross-version tests for aggregated modules.
- `sbt "scala-relay-core/test"` runs tests for a single module.
- `sbt "+ publishLocal; ++2.12.21 scriptedAll"` publishes local artifacts and runs all sbt scripted tests, matching CI for Scala 2.12.
- `sbt scalafmtAll scalafmtSbt` formats Scala and sbt files using `.scalafmt.conf`.
- `sbt publishLocalAll` publishes all locally useful artifacts via the project alias.
- `sbt scala-relay-ijext/updateIntellij` updates IntelliJ plugin support files before release-related work.

Use JDK 17. CI also uses Node 22 for Relay compiler fixture coverage.

## Coding Style & Naming Conventions

Follow Scalafmt 3.9.9 with `runner.dialect = scala213source3`, `maxColumn = 130`, sorted imports, and Scala 3 overrides for `scala-3` paths. Prefer existing package structure under `com.goodcover.relay`. Keep generated Relay types and fixture expectations aligned with current naming patterns, for example `TestQuery.expected.scala`, `Test_fragment.scala`, and GraphQL files in `src/main/resources/graphql/`.

## Testing Guidelines

Unit tests use uTest (`utest.runner.Framework`). Name test files with `*Test.scala` where practical, and keep version-specific macro tests in the matching `scala-3` or Scala 2 source tree. For codegen changes, update or add scripted fixtures and expected Scala output under `sbt-scala-relay/src/sbt-test/`. Run at least the affected module tests, plus scripted tests when changing sbt tasks, Relay conversion, resource handling, or generated output.

## Commit & Pull Request Guidelines

Recent commits are short, imperative summaries such as `Use a simpler non-confusing term`, plus release/version commits like `Setting version to 0.46.3` and Scala Steward dependency updates. Keep commits focused and avoid unrelated formatting churn. Pull requests should describe the behavior change, list tests run, link related issues when applicable, and call out generated fixture updates or release implications.

## Release & Configuration Notes

Releases use `sbt release`, signed publishing, Sonatype credentials, and GitHub release automation. Do not commit local credentials, GPG material, or generated build output from `target/`, `out/`, or IDE caches.
