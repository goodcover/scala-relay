#!/usr/bin/env bash

set -euxo pipefail

# Don't use inline because macOS has different options.
sed "s|\${PWD}|$(readlink -f "$(pwd)")|g" expected.template.graphql > expected.graphql
