#!/bin/sh

set -e

cd $(dirname $0)

cmd="$@"

# Build all models
bazel build //models

# Now copy all generated typescript files
find ../bazel-bin/models/ -type f -path '**/models/**/*.ts' -exec /bin/cp -rf '{}' ./src/generated/protobuf ';'
chmod 777 -R ./src/generated/protobuf

exec $cmd