#!/bin/sh

set -e

DIR=$(dirname $0)
cd $DIR
cmd="$@"

if [ -z "$SKIPPROTOCOMPILATION" ]
then
    # Build all models
    # ../run.sh build //models
    echo "Compiling protos"
    bazel build //models

    # Now copy all generated typescript files
    # find ../bazel-bin/models/ -type f -path '**/models/**/*.js'
    # find ../bazel-bin/models/ -type f -path '**/models/**/*.ts'
    rm -rf $DIR/src/generated/protobuf
    echo "Creating $DIR/src/generated/protobuf"
    mkdir -p $DIR/src/generated/protobuf
    find ../bazel-bin/models/ -type f -path '**/models/**/*.ts' -exec /bin/cp -rf '{}' $DIR/src/generated/protobuf ';'
    find ../bazel-bin/models/ -type f -path '**/models/**/*.js' -exec /bin/cp -rf '{}' $DIR/src/generated/protobuf ';'
    chmod 777 -R $DIR/src/generated/protobuf
fi

exec $cmd