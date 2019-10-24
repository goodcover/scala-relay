#!/bin/sh

set -xe

echo $PGP_PUBLIC
echo "$PGP_SECRET" | gpg --import