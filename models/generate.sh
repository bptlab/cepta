#!/bin/sh

cd $(dirname $0)
OUT=$(realpath ./out)
echo $OUT
rm -r $OUT/*

# https://github.com/danielvladco/go-proto-gql
# go install github.com/danielvladco/go-proto-gql/protoc-gen-gql
# go install github.com/danielvladco/go-proto-gql/protoc-gen-gogqlgen
# go install github.com/danielvladco/go-proto-gql/protoc-gen-gqlgencfg
# export PATH=${PATH}:${GOPATH}/bin

# bazel run //:gazelle -- update-repos -from_file=go.mod
# bazel run //:gazelle -- update-repos "github.com/sirupsen/logrus"

# go get -u github.com/golang/protobuf/protoc-gen-go
# go install github.com/golang/protobuf/protoc-gen-go

# Compile graphql schemas using gql plugin
protoc -I=. ./service/gql/*.proto --gql_out=$OUT

# Compile grpc schemas using grpc plugin
protoc -I=. ./service/grpc/*.proto --go_out=plugins=grpc:$OUT

# Compile basic types to go (, java and python?) stubs
protoc -I=. ./types/*.proto --go_out=$OUT