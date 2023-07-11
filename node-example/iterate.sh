#!/bin/sh

set -xe

pushd ../node-compiler && yarn build && yarn link
popd
yarn link relay-compiler-language-scalajs
node --inspect-brk ./node_modules/.bin/relay-compiler --language scalajs --schema ./schema.graphql --src src/ --artifactDirectory target/ --customScalars.Protobuf=com.pack.Class
