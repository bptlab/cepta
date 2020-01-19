load("@bazel_gazelle//:def.bzl", "gazelle")

# gazelle:prefix github.com/bptlab/cepta
gazelle(name = "gazelle")

load("@rules_proto_grpc//:plugin.bzl", "proto_plugin")

proto_plugin(
    name = "proto_gql_plugin",
    options = [
        "paths=source_relative",
        "plugins=gql",
    ],
    protoc_plugin_name = "gql",
    outputs = ["{protopath}.pb.graphqls"],
    # out = "{protopath}.pb.graphqls",
    # tool = "@com_github_golang_protobuf//protoc-gen-go",
    # tool = Label("//:protoc-gen-gql"),
    # tool = "@com_github_go_proto_gql_danielvladco//protoc-gen-gql",
    tool = "@go_proto_gql//:protoc-gen-go",
    exclusions = [
        "google/api",
        "google/protobuf",
    ],
    visibility = ["//visibility:public"],
)
