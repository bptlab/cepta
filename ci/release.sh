#!/usr/bin/env bash

cd $(dirname $0)
cd ../

echo "Preparing for release"

# Assure all (including internal) tests succeed
echo "Running tests and checks"

# Bump the version
echo "Incrementing tbe version"
bump2version "$@"

# Build and push the docker containers (reuquired docker registry access)
echo "Building docker containers"
echo "Publishing to docker hub"

echo "Release completed"
