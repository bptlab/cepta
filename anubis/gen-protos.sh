#!/bin/sh

set -e

cd $(dirname $0)

cmd="$@"

# Build all models
# ../run.sh build //models
bazel build //models

# Now copy all generated typescript files
# find ../bazel-bin/models/ -type f -path '**/models/**/*.js'
# find ../bazel-bin/models/ -type f -path '**/models/**/*.ts'
rm ./src/generated/protobuf/*
find ../bazel-bin/models/ -type f -path '**/models/**/*.ts' -exec /bin/cp -rf '{}' ./src/generated/protobuf ';'
find ../bazel-bin/models/ -type f -path '**/models/**/*.js' -exec /bin/cp -rf '{}' ./src/generated/protobuf ';'
chmod 777 -R ./src/generated/protobuf

exec $cmd