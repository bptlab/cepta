#!/usr/bin/env bash

cd $(dirname $0)
cd compose

if [ -z "$BUILD" ]; then
  echo "Using existing images. To rebuild, run:"
  echo ""
  echo "  BUILD=1 deployment/dev/devenv.sh ..args"
  echo ""
else
  if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    bazel run //osiris/usermgmt:build-image --platforms=@io_bazel_rules_go//go/toolchain:linux_amd64
    bazel run //osiris/notification:build-image --platforms=@io_bazel_rules_go//go/toolchain:linux_amd64
    bazel run //osiris/auth:build-image --platforms=@io_bazel_rules_go//go/toolchain:linux_amd64
    bazel run //auxiliary/producers/replayer:build-image --platforms=@io_bazel_rules_go//go/toolchain:linux_amd64
  else
    # Build local images first
    bazel run //:build-images
  fi
fi


if [[ "$OSTYPE" == "darwin"* ]]; then
  # macOS
  export ENVOY_HOST=host.docker.internal
  export ENVOY_NETWORK_MODE=bridge
else
  # Assume we are running under linux
  export ENVOY_HOST=localhost
  export ENVOY_NETWORK_MODE=host
  # This seems to be not enough
  # export ENVOY_HOST=$(ip -4 addr show docker0 | grep -Po 'inet \K[\d.]+')
fi

echo "Using docker host at ${ENVOY_HOST}"

docker-compose --compatibility \
  -f core.compose.yml \
  -f cockpit.compose.yml \
  -f envoy.compose.yml \
  -f mongo.compose.yml \
  -f anubis.compose.yml \
  -f redis.compose.yml \
  -f monitoring.compose.yml \
  "$@"