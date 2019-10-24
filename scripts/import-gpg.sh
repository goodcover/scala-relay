#!/bin/sh

set -xe

echo $PGP_PUBLIC
echo "$PGP_SECRET" | base64 --decode | gpg --import