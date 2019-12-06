#!/usr/bin/env bash

cd $(dirname $0)
cd compose
# -f postgresimporter.compose.yml -f frontend.compose.yml \
docker-compose \
  -f core.compose.yml \
  -f cockpit.compose.yml \
  -f envoy.compose.yml \
  -f rabbitmq.compose.yml \
  "$@"