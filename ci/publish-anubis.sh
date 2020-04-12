#!/usr/bin/env bash

export CEPTA_ROOT=$(dirname "${BASH_SOURCE}")/..
cd $CEPTA_ROOT

docker build -t anubis -f anubis/Dockerfile .
docker tag anubis ceptaorg/anubis:"$@"
docker push ceptaorg/anubis:"$@"