#!/bin/sh

set -xe

pushd ../node-compiler && yarn build
popd
yarn add file:../node-compiler
./node_modules/.bin/relay-compiler --language scalajs --schema ./schema.graphql --src src/ --artifactDirectory target/
