#!/usr/bin/env bash

cd $(dirname $0)
cd compose
echo "Starting flink cluster dev environment"
docker-compose \
  -f cepta.compose.yml \
  -f cockpit.compose.yml \
  -f frontend.compose.yml \
  -f backend.compose.yml \
  -f producers.compose.yml \
  -f flink.compose.yml \
  -f kafka.compose.yml \
  -f envoy.compose.yml \
  -f postgres.compose.yml \
  -f rabbitmq.compose.yml \
  up -d --scale flink-taskmanager=2 "$@"
