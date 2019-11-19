#!/usr/bin/env bash

cd $(dirname $0)
cd compose
echo "Starting flink cluster dev environment"
docker-compose \
  -f cockpit.compose.yml \
  -f flink.compose.yml \
  -f kafka.compose.yml \
  -f postgres.compose.yml \
  up -d --scale flink-taskmanager=2 "$@"
