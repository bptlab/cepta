#!/usr/bin/env bash

cd $(dirname $0)
echo "Stopping flink cluster prod environment"
docker-compose down "$@"