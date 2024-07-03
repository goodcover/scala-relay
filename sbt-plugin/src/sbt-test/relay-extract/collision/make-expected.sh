#!/usr/bin/env bash

set -euxo pipefail

# Don't use inline because macOS has different options.
sed "s|\${PWD}|$(readlink -f "$(pwd)")|g" expected1.template.graphql > expected1.graphql
sed "s|\${PWD}|$(readlink -f "$(pwd)")|g" expected2.template.graphql > expected2.graphql
