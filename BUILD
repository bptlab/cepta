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
    tool = "@go_proto_gql//protoc-gen-gql",
    exclusions = [
        "google/api",
        "google/protobuf",
    ],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "cepta",
    srcs = [
        "//osiris",
        "//core",
        "//aux",
    ],
)
