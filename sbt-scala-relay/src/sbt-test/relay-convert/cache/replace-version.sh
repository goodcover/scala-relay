#!/usr/bin/env bash

set -euxo pipefail

if [[ "$OSTYPE" == "darwin"* ]]; then
  sed -E -i '' 's/"version":[0-9]+/"version":0/g' target/streams/compile/relayConvert/_global/streams/relay/convert/last
else
  sed -E -i 's/"version":[0-9]+/"version":0/g' target/streams/compile/relayConvert/_global/streams/relay/convert/last
fi
