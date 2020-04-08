#!/usr/bin/env bash

cd $(dirname $0)
cd ../

echo "Preparing for release"

# Assure all internal tests succeed
echo "Running tests and checks"
bazel test :internal --test_output=errors
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi

# Bump the version
echo "Incrementing the version"
bump2version "$@"
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi

# Build and push the docker containers (requires docker registry access)
echo "Building and publishing docker images to docker hub"
bazel run :publish
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi

echo "Release completed"
