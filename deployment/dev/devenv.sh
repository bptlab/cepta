#!/usr/bin/env bash

cd $(dirname $0)
cd compose

# Build local images first
bazel run //:build-images

docker-compose \
  -f core.compose.yml \
  -f cockpit.compose.yml \
  -f envoy.compose.yml \
  -f mongo.compose.yml \
  -f anubis.compose.yml \
  -f rabbitmq.compose.yml \
  -f monitoring.compose.yml \
  "$@"