#!/usr/bin/env bash

cd $(dirname $0)
cd compose
docker-compose \
  -f core.compose.yml \
  -f cockpit.compose.yml \
  -f envoy.compose.yml \
  -f frontend.compose.yml \
  -f _backend.compose.yml \
  -f rabbitmq.compose.yml \
  -f postgresimporter.compose.yml \
  "$@"