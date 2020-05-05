#!/usr/bin/env bash

cd $(dirname $0)
cd compose

if [ -z "$BUILD" ]; then
  echo "Using existing images. To rebuild, run:"
  echo ""
  echo "  BUILD=1 deployment/dev/devenv.sh ..args"
  echo ""
else
  # Build local images first
  bazel run //:build-images
fi

docker-compose --compatibility \
  -f core.compose.yml \
  -f cockpit.compose.yml \
  -f envoy.compose.yml \
  -f mongo.compose.yml \
  -f anubis.compose.yml \
  -f redis.compose.yml \
  -f monitoring.compose.yml \
  "$@"