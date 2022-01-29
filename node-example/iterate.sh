#!/bin/sh

set -xe

pushd ../node-compiler && yarn build
popd
yarn add file:../node-compiler
node ./node_modules/.bin/relay-compiler --language scalajs --schema ./schema.graphql --src src/ --artifactDirectory target/ --customScalars.Protobuf=com.pack.Class
