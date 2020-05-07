#!/bin/bash

set -e

export PROTO_ROOT_REGEX="models/.*"
export DIR=$(dirname $0)

cd $DIR
cmd="$@"

if [ -z "$SKIPPROTOCOMPILATION" ]
then
    # Build all models
    echo "Compiling protos";
    bazel build //models:models;

    # Now copy all generated typescript files
    # find ../bazel-bin/models/ -type f -path '**/models/**/*.js'
    # find ../bazel-bin/models/ -type f -path '**/models/**/*.ts'
    rm -rf $DIR/src/generated/protobuf
    echo "Creating $DIR/src/generated/protobuf"
    mkdir -p $DIR/src/generated/protobuf
    # find ../bazel-bin/models/ -type f -path '**/models/**/*.ts' -exec sh -c "echo {}" ';'
    find ../bazel-bin/models/ -type f -path '**/models/**/*.ts' -exec sh -c 'export PROTO_PATH=./src/generated/protobuf/$(echo {} | python reg.py "$PROTO_ROOT_REGEX") && mkdir -p $(dirname $PROTO_PATH) && /bin/cp -rf {} $PROTO_PATH' ';'
    find ../bazel-bin/models/ -type f -path '**/models/**/*.js' -exec sh -c 'export PROTO_PATH=./src/generated/protobuf/$(echo {} | python reg.py "$PROTO_ROOT_REGEX") && mkdir -p $(dirname $PROTO_PATH) && /bin/cp -rf {} $PROTO_PATH' ';'
    chmod -R 777 src/generated/protobuf
fi

exec $cmd