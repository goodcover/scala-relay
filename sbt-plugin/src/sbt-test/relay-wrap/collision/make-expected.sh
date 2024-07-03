#!/usr/bin/env bash

set -euxo pipefail

# Don't use inline because macOS has different options.
sed "s|\${PWD}|$(readlink -f "$(pwd)")|g" expected1.template.js > expected1.js
sed "s|\${PWD}|$(readlink -f "$(pwd)")|g" expected2.template.js > expected2.js
