#!/bin/sh

set -xe

pushd ../node-compiler && yarn build
popd
node ./node_modules/.bin/relay-compiler --language scalajs --schema ./schema.graphql --src src/ --artifactDirectory target/ --customScalars.Protobuf=com.pack.Class

pushd ../node-compiler-text && yarn build
popd
node ./node_modules/.bin/relay-compiler --language scala2gql --schema ./schema.graphql --src src/ --artifactDirectory target2/ --customScalars.Protobuf=com.pack.Class
