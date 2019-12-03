#!/usr/bin/env bash

cd $(dirname $0)
echo "Starting flink cluster prod environment"
docker-compose up -d --scale flink-taskmanager=2 "$@"
